package dslabs.primarybackup;

import static dslabs.primarybackup.ClientTimer.CLIENT_RETRY_MILLIS;

import dslabs.atmostonce.AMOCommand;
import dslabs.framework.Address;
import dslabs.framework.Client;
import dslabs.framework.Command;
import dslabs.framework.Node;
import dslabs.framework.Result;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
class PBClient extends Node implements Client {
  private final Address viewServer;

  // Your code here...
  private Command command;
  private Result result;
  private int sequenceNum;
  private View view;


  /* -----------------------------------------------------------------------------------------------
   *  Construction and Initialization
   * ---------------------------------------------------------------------------------------------*/
  public PBClient(Address address, Address viewServer) {
    super(address);
    this.viewServer = viewServer;
    this.sequenceNum = 0;
  }

  @Override
  public synchronized void init() {
    // Your code here...
    this.send(new GetView(), this.viewServer);
  }

  /* -----------------------------------------------------------------------------------------------
   *  Client Methods
   * ---------------------------------------------------------------------------------------------*/
  @Override
  public synchronized void sendCommand(Command command) {
   //System.out.println("sendCommand " + command + " view: " + view);

    if (command != null) {
      this.command = command;
      this.result = null;
      if (this.view == null || this.view.viewNum() == 0) {
        send(new GetView(), this.viewServer);
        this.set(new GetViewTimer(), CLIENT_RETRY_MILLIS);
      } else {
        AMOCommand c = new AMOCommand(command, sequenceNum, this.address());
        //System.out.println("client command = " + c);
        this.send(new Request(c, this.view.viewNum()), view.primary());
        this.set(new ClientTimer(this.sequenceNum), CLIENT_RETRY_MILLIS);
      }
    }
    // Your code here...
  }

  @Override
  public synchronized boolean hasResult() {
    // Your code here...
    return this.result != null;
  }

  @Override
  public synchronized Result getResult() throws InterruptedException {
    // Your code here...
    while (this.result == null) {
      this.wait();
    }

    return this.result;
  }

  /* -----------------------------------------------------------------------------------------------
   *  Message Handlers
   * ---------------------------------------------------------------------------------------------*/
  private synchronized void handleReply(Reply m, Address sender) {
    //System.out.println("\nhandleReply +" + m + "sender = " + sender);
    // Your code here...
    if (m.amoResult().sequenceNum() == this.sequenceNum && sender == this.view.primary()) {
      this.result = m.amoResult().result();
      ////System.out.println("result +" + this.result);
      this.sequenceNum++;
      this.notify();


    }
  }

  private synchronized void handleViewReply(ViewReply m, Address sender) {
    //System.out.println("handleViewReply Client " + m);
    // Your code here...
    if (this.view == null) {
      ////System.out.println("a   ");
      this.view = m.view();
      if (command != null) {
        sendCommand(command);
      }
    } else if (m.view().viewNum() > this.view.viewNum()) {
      ////System.out.println("b   ");
      this.view = m.view();
    }
  }

  // Your code here...

  /* -----------------------------------------------------------------------------------------------
   *  Timer Handlers
   * ---------------------------------------------------------------------------------------------*/
  private synchronized void onClientTimer(ClientTimer t) {
    //System.out.println("onClientTimer");
    // Your code here...
    // maybe changfe if to use object.equals
    if (this.sequenceNum == t.sequenceNum() && this.result == null) {
      AMOCommand c = new AMOCommand(command, sequenceNum, this.address());
      this.send(new Request(c, this.view.viewNum()), this.view.primary());
      this.send(new GetView(), this.viewServer);
      this.set(t, CLIENT_RETRY_MILLIS);
    }
  }

  private synchronized void onGetViewTimer(GetViewTimer t) {
    if (this.view == null || this.view.viewNum() == 0) {
      this.send(new GetView(), this.viewServer);
      this.set(t, CLIENT_RETRY_MILLIS);
    }
  }



}
