package dslabs.clientserver;

import static dslabs.clientserver.ClientTimer.CLIENT_RETRY_MILLIS;

import com.google.common.base.Objects;
import dslabs.atmostonce.AMOCommand;
import dslabs.atmostonce.AMOResult;
import dslabs.framework.Address;
import dslabs.framework.Client;
import dslabs.framework.Command;
import dslabs.framework.Node;
import dslabs.framework.Result;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Simple client that sends requests to a single server and returns responses.
 *
 * <p>See the documentation of {@link Client} and {@link Node} for important implementation notes.
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
class SimpleClient extends Node implements Client {
  private final Address serverAddress;

  // Your code here...
  // ????????????????????
  private Request request;
  private Reply reply;
  private int seqNum;

  /* -----------------------------------------------------------------------------------------------
   *  Construction and Initialization
   * ---------------------------------------------------------------------------------------------*/
  public SimpleClient(Address address, Address serverAddress) {
    super(address);
    this.serverAddress = serverAddress;
    seqNum = 0;
  }

  @Override
  public synchronized void init() {
    // No initialization necessary
  }

  /* -----------------------------------------------------------------------------------------------
   *  Client Methods
   * ---------------------------------------------------------------------------------------------*/
  @Override
  public synchronized void sendCommand(Command command) {
    // Your code here...
    //if (!(command instanceof Command)) {
      //throw new IllegalArgumentException();
    //}

    AMOCommand com = new AMOCommand(command, seqNum++, address());
    request = new Request(com);
    reply = null;

    this.send(request, serverAddress);
    this.set(new ClientTimer(request), CLIENT_RETRY_MILLIS);
  }


  @Override
  public synchronized boolean hasResult() {
    // Your code here..
    return reply != null;
  }

  @Override
  public synchronized Result getResult() throws InterruptedException {
    // Your code here...
    while (reply == null) {
      this.wait();
    }

    return reply.result().result();
  }

  /* -----------------------------------------------------------------------------------------------
   *  Message Handlers
   * ---------------------------------------------------------------------------------------------*/
  private synchronized void handleReply(Reply m, Address sender) {
    // Your code here...
    if (Objects.equal(request.command().sequenceNum(), m.result().sequenceNum())) {
      reply = m;
      notify();
    }
  }

  /* -----------------------------------------------------------------------------------------------
   *  Timer Handlers
   * ---------------------------------------------------------------------------------------------*/
  private synchronized void onClientTimer(ClientTimer t) {
    // Your code here...
    if (Objects.equal(request, t.request()) && reply == null) {
      send(request, serverAddress);
      set(t, CLIENT_RETRY_MILLIS);
    }
  }
}
