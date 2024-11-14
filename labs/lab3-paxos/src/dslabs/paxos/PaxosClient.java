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
  private PaxosRequest request;
  private Result result;
  private int sequenceNum;

  /* -----------------------------------------------------------------------------------------------
   *  Construction and Initialization
   * ---------------------------------------------------------------------------------------------*/
  public PaxosClient(Address address, Address[] servers) {
    super(address);
    this.servers = servers;
    sequenceNum = 0;
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
    // Your code here...
    AMOCommand com = new AMOCommand(command, sequenceNum, address());
    request = new PaxosRequest(com);
    result = null;

    for (Address server : servers) {
      this.send(request, server);
    }
    this.set(new ClientTimer(sequenceNum), CLIENT_RETRY_MILLIS);
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
    if (request.command().sequenceNum() == m.result().sequenceNum()) {
      result = m.result();
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
    if (request.command().sequenceNum() == t.sequenceNumber() && result == null) {
      for (Address server : servers) {
        this.send(request, server);
      }
      set(t, CLIENT_RETRY_MILLIS);
    }
  }
}
