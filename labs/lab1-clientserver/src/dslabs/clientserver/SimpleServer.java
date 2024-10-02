package dslabs.clientserver;

import dslabs.atmostonce.AMOApplication;
import dslabs.atmostonce.AMOCommand;
import dslabs.atmostonce.AMOResult;
import dslabs.framework.Address;
import dslabs.framework.Application;
import dslabs.framework.Node;
import dslabs.framework.Result;
import dslabs.kvstore.KVStore;
import java.util.HashMap;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Simple server that receives requests and returns responses.
 *
 * <p>See the documentation of {@link Node} for important implementation notes.
 */

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
class SimpleServer extends Node {
  // Your code here...
  private final Application app;

  /* -----------------------------------------------------------------------------------------------
   *  Construction and Initialization
   * ---------------------------------------------------------------------------------------------*/
  public SimpleServer(Address address, Application app) {
    super(address);

    // Your code here...
    this.app = new AMOApplication<>(app, new HashMap<Address, AMOResult>(),
                                         new HashMap<Address, Integer>());
  }

  @Override
  public void init() {
    // No initialization necessary
  }

  /* -----------------------------------------------------------------------------------------------
   *  Message Handlers
   * ---------------------------------------------------------------------------------------------*/
  private void handleRequest(Request m, Address sender) {
    // Your code here...
    /*
    Result r;
    if (m.command() instanceof AMOCommand) {
      r = app.execute(m.command().command());
    } else {
      r = app.execute(m.command());
    }
    */
    Result r = app.execute(m.command());
    //AMOResult amoResult = new AMOResult(r, m.command().sequenceNum() ,m.command().clientAddress());

    Reply rep = new Reply((AMOResult) r);
    send(rep, sender);
  }
}
