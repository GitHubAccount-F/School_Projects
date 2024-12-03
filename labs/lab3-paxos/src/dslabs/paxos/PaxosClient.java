package dslabs.paxos;

import static dslabs.paxos.ClientTimer.CLIENT_RETRY_MILLIS;

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
public final class PaxosClient extends Node implements Client {
  private final Address[] servers;

  // Your code here...
  private Command command;
  // private PaxosRequest request;
  private Result result;
  private int sequenceNum;

  /* -----------------------------------------------------------------------------------------------
   *  Construction and Initialization
   * ---------------------------------------------------------------------------------------------*/
  public PaxosClient(Address address, Address[] servers) {
    super(address);
    this.servers = servers;
    sequenceNum = 0;
    command = null;
    result = null;
    ////// System.out.println(this.servers);
  }

  @Override
  public synchronized void init() {
    // No need to initialize
  }

  /* -----------------------------------------------------------------------------------------------
   *  Client Methods
   * ---------------------------------------------------------------------------------------------*/
  @Override
  public synchronized void sendCommand(Command operation) {
    //// System.out.println("CLIENT sendCommand");
    // Your code here...
    if (operation != null) {
      this.command = operation;
      AMOCommand com = new AMOCommand(this.command, this.sequenceNum, this.address());
      PaxosRequest req = new PaxosRequest(com);
      result = null;

      for (Address server : servers) {
        this.send(req, server);
      }
      this.set(new ClientTimer(sequenceNum), CLIENT_RETRY_MILLIS);
    }
  }

  @Override
  public synchronized boolean hasResult() {
    // Your code here...
    return result != null;
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
   * Message Handlers
   * ---------------------------------------------------------------------------------------------*/
  private synchronized void handlePaxosReply(PaxosReply m, Address sender) {
    // Your code here...
    /*
    verify PaxosReply sequence number matches our current.
      update result
      increment seqNum (not needed; this is done in sendCommand)
      notify()
     */
    //// System.out.println("CLIENT handlePaxosReply  " + m + " and seq = " + sequenceNum);
    if (this.sequenceNum == m.result().sequenceNum()) {
      // System.out.println("CLIENT handlePaxosReply  " + m + " and seq = " + sequenceNum);
      result = m.result().result();
      this.sequenceNum++;
      notify();
    }
  }

  /* -----------------------------------------------------------------------------------------------
   *  Timer Handlers
   * ---------------------------------------------------------------------------------------------*/
  private synchronized void onClientTimer(ClientTimer t) {
    // Your code here...
    /*
    if client timer.sequence number matches our current seqNum and result still == null
      resend command to all servers in servers list
      reset timer
     */
    if (this.sequenceNum == t.sequenceNumber() && result == null) {
      AMOCommand com = new AMOCommand(this.command, this.sequenceNum, this.address());
      PaxosRequest req = new PaxosRequest(com);
      for (Address server : servers) {
        this.send(req, server);
      }
      set(t, CLIENT_RETRY_MILLIS);
    }
  }
}
