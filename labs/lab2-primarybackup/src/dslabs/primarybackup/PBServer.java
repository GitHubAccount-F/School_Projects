package dslabs.primarybackup;

import static dslabs.primarybackup.PingTimer.PING_MILLIS;
import static dslabs.primarybackup.StateTransferTimer.STATE_TRANSFER_RETRY_MILLIS;

import com.google.common.base.Objects;
import dslabs.atmostonce.AMOApplication;
import dslabs.atmostonce.AMOResult;
import dslabs.framework.Address;
import dslabs.framework.Application;
import dslabs.framework.Node;
import dslabs.framework.Result;
import java.util.HashMap;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
class PBServer extends Node {
  private final Address viewServer;

  // Your code here...
  //private boolean stopTakingRequest; // for when we get a view change, and the server is not the primary
  private boolean stateReceived; // starts false, made true when this server receives state from another server, and once true prevents this server from executing another state transfer received!
  private boolean stateTransferInProgress; // false by default, turns true when this server is sending its state to another server, while true this server doesn't accept PBClient requests
  private View view;
  private Application application;

  /* -----------------------------------------------------------------------------------------------
   *  Construction and Initialization
   * ---------------------------------------------------------------------------------------------*/
  PBServer(Address address, Address viewServer, Application app) {
    super(address);
    this.viewServer = viewServer;
    this.application = new AMOApplication<>(app, new HashMap<>(), new HashMap<>());

    // Your code here...
    this.view = null;
    this.stateReceived = false;
    this.stateTransferInProgress = false;
   // this.stopTakingRequest = false;
  }

  @Override
  public void init() {
    // Your code here...
    this.send(new Ping(0), this.viewServer);
    this.set(new PingTimer(), PING_MILLIS);
  }

  /* -----------------------------------------------------------------------------------------------
   *  Message Handlers
   * ---------------------------------------------------------------------------------------------*/
  private void handleRequest(Request m, Address sender) {
    if (this.view != null && this.view.primary() == this.address() && this.view.viewNum() == m.viewNum() &&
        !this.stateTransferInProgress) {
      // LOGIC HERE: Check if already executed this message, if so send stored result, otherwise forward request to backup --> execute --> store result --> send result as reply
      if (this.view.backup() != null) { // checks to see if backup is null
        ForwardRequest x = new ForwardRequest(m);
        send(x, this.view.backup());
      } else { // if null, execute only for primary
        Result r = application.execute(m.amoCommand());
        Reply rep = new Reply((AMOResult) r, m.viewNum());
        send(rep, sender);
      }
      // send to backup with forward request, handle forward reply
    }
    // nothing happens outside the above if statement; sender's ClientTimer will fire triggering it to resend / call GetView so it will update to the right server to send to if needed
  }

  private void handleViewReply(ViewReply m, Address sender) {
    if (this.view == null) { // start of server
      this.view = m.view();
    } else if (m.view().viewNum() > this.view.viewNum()) {
      this.stateReceived = false;
      this.stateTransferInProgress = false;
      this.view = m.view();
      //this.stopTakingRequest = (this.view.primary() != this.address());
      if (this.view.backup() != null) { // handle state transfer logic here
        // if you are the primary
        if (this.view.primary() == this.address()) {
          stateTransferInProgress = true;
          send(new StateTransfer(application, this.view), this.view.backup());
          this.set(new StateTransferTimer(), STATE_TRANSFER_RETRY_MILLIS);
        }
      }
    }
  }

  // Your code here...

  // For the backup
  // no need to make a new timer for this, we put the burden on the client
  private void handleForwardRequest(ForwardRequest m, Address sender) {
    if (this.view.backup() == this.address() &&
        m.request().viewNum() == this.view.viewNum() &&
        this.application != null && stateReceived) {
      Result r = application.execute(m.request().amoCommand());  // maybe compare to primary result
      ForwardReply rep = new ForwardReply(true, m);
      send(rep, sender);
    }
  }

  // For the primary
  private void handleForwardReply(ForwardReply m, Address sender) {
    if (this.view.primary() == this.address() && m.success() && sender == this.view.backup()
    && !this.stateTransferInProgress) {
      Result r = application.execute(m.forwardRequest().request().amoCommand());
      Request y = m.forwardRequest().request();
      Reply rep = new Reply((AMOResult) r, y.viewNum());
      // send to client
      send(rep, y.amoCommand().clientAddress());
    }
  }

  /* -----------------------------------------------------------------------------------------------
   *  Timer Handlers
   * ---------------------------------------------------------------------------------------------*/
  private void onPingTimer(PingTimer t) {
    if (this.view == null) {
      this.send(new Ping(0), this.viewServer);
    } else {
      this.send(new Ping(this.view.viewNum()), this.viewServer);
    }
    this.set(t, PING_MILLIS);
  }

  // Your code here...

  /* -----------------------------------------------------------------------------------------------
   *  Utils
   * ---------------------------------------------------------------------------------------------*/
  // Your code here...
  private void onStateTransferTimer(StateTransferTimer t) {
    // state transfer is still happening, so resend timer and state
    if (this.stateTransferInProgress && this.view.primary() == this.address() && this.view.backup() != null) {
      send(new StateTransfer(application, this.view), this.view.backup());
      this.set(t, STATE_TRANSFER_RETRY_MILLIS);
    }
  }

  private void handleStateTransfer(StateTransfer state, Address sender) {
    if ((this.view == null || (this.view.viewNum() <= state.view().viewNum()))) {
      if (!stateReceived) {
        this.application = state.application();
        this.view = state.view();
        stateReceived = true;
      }
      StateTransferAck ack = new StateTransferAck(true, this.view);
      send(ack, sender);
    }

  }

  private void handleStateTransferAck(StateTransferAck ack, Address sender) {
    if (sender == this.view.backup()) {
      if(ack.success()) {
        stateTransferInProgress = false;
      }
    }

  }

}
