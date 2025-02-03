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
import java.util.LinkedList;
import java.util.Queue;
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
  private AMOApplication application;


  //private Request lastReq;
  //private Queue<Request> lock;
  private Request currentRequest;
// private boolean currentBoolean;


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
    //lastReq = null;
    //lock = new LinkedList<>();
    currentRequest = null;
    //currentBoolean = false;
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
    //System.out.println("handleRequest + " + m);
    //System.out.println("  curQeq is + " + currentRequest);
    if (this.view != null && this.view.primary() == this.address() && this.view.viewNum() == m.viewNum() &&
        !this.stateTransferInProgress) {
      //System.out.println("  entered");
      // if we already executed the request, then execute it on primary and send back result
      // If we are already processing a request, ignore new ones


      if (application.alreadyExecuted(m.amoCommand())) {
        //System.out.println("  here1");
        AMOResult r = application.execute(m.amoCommand());
        send(new Reply(r, m.viewNum()), sender);
      } else if (this.currentRequest == null || currentRequest.equals(m)) {
        currentRequest = m;
        //currentBoolean = false;
        if (this.view.backup() != null) { // checks to see if backup is null
          // send forward request to the backup
          //System.out.println("  here2");
          send(new ForwardRequest(m), this.view.backup());
        } else { // if no backup, execute only on primary
          //System.out.println("  here3");
          AMOResult r = application.execute(m.amoCommand());
          send(new Reply(r, m.viewNum()), sender);
          currentRequest = null;
        }
      }
    }
  }




  // amo app check if request was already executed
  // if not , handle one
  private void handleViewReply(ViewReply m, Address sender) {
    //System.out.println("handleViewReply + " + m);
    //System.out.println("view is + " + this.view);
    if (this.view == null || m.view().viewNum() > this.view.viewNum()) { // we receive a higher view number
      this.stateReceived = false;
      //lock = null;
      this.stateTransferInProgress = false;
      this.view = m.view();
      if (this.currentRequest != null && currentRequest.viewNum() != this.view.viewNum()) {
        currentRequest = null;
      }
      if (this.view.backup() != null && this.view.primary() == this.address()) {
        this.stateTransferInProgress = true;
        send(new StateTransfer(application, this.view), this.view.backup());
        this.set(new StateTransferTimer(), STATE_TRANSFER_RETRY_MILLIS);
      }
    }
  }


  // Your code here...


  // For the backup
  // no need to make a new timer for this, we put the burden on the client
  private void handleForwardRequest(ForwardRequest m, Address sender) {
    //System.out.println("handleForwardRequest");
    if (this.view.backup() == this.address() &&
        m.request().viewNum() == this.view.viewNum() &&
        stateReceived) {
      Result r = application.execute(m.request().amoCommand());  // maybe compare to primary result
      ForwardReply rep = new ForwardReply(true, m);
      send(rep, sender);
    }
  }


  // For the primary
  private void handleForwardReply(ForwardReply m, Address sender) {
    //System.out.println("handleForwardReply");
    if (this.view.primary() == this.address() && sender == this.view.backup()
        && (this.currentRequest != null &&
        this.currentRequest.equals(m.forwardRequest().request()))) {
      AMOResult r = application.execute(m.forwardRequest().request().amoCommand());
      send(new Reply(r,currentRequest.viewNum()), currentRequest.amoCommand().clientAddress());
      currentRequest = null;
    }
  }


  /* -----------------------------------------------------------------------------------------------
   *  Timer Handlers
   * ---------------------------------------------------------------------------------------------*/
  private void onPingTimer(PingTimer t) {
    if (this.view == null) {
      this.send(new Ping(0), this.viewServer);
    } else {
      if (!(this.view.primary() == this.address() && this.stateTransferInProgress)) {
        this.send(new Ping(this.view.viewNum()), this.viewServer);
      }
    }
    this.set(t, PING_MILLIS);
  }


  // Your code here...


  /* -----------------------------------------------------------------------------------------------
   *  Utils
   * ---------------------------------------------------------------------------------------------*/
  // Your code here...
  private void onStateTransferTimer(StateTransferTimer t) {
    //System.out.println("handleStateTransferTimer");
    // state transfer is still happening, so resend timer and state
    if (this.stateTransferInProgress && this.view.primary() == this.address() && this.view.backup() != null) {
      send(new StateTransfer(application, this.view), this.view.backup());
      this.set(t, STATE_TRANSFER_RETRY_MILLIS);
    }
  }


  private void handleStateTransfer(StateTransfer state, Address sender) {
    //System.out.println("handleStateTransfer");
    /*
   if (this.view == null || (state.view().viewNum() == this.view.viewNum() && stateTransferInProgress) ||
       (state.view().viewNum() > this.view.viewNum() && !stateTransferInProgress)) {
     if (!stateReceived || state.view().viewNum() > this.view.viewNum()) {
       this.application = state.application();
       this.view = state.view();
       //this.mechanism = state.mechanism();
       stateReceived = true;
       stateTransferInProgress = false;
     }
     StateTransferAck ack = new StateTransferAck(true, this.view);
     send(ack, sender);
   }
    ////System.out.println("handleForwardRequest");
    */
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
    //System.out.println("handleStateTransferAck");
    if (sender == this.view.backup() && this.stateTransferInProgress) {
      if(ack.success()) {
        stateTransferInProgress = false;
      }
    }


  }


}


/*
Look at incoming req, make sure it was not old request
Locking mech. Store a request






*/

