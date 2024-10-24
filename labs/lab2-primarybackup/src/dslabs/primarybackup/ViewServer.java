package dslabs.primarybackup;

import static dslabs.primarybackup.PingCheckTimer.PING_CHECK_MILLIS;

import dslabs.framework.Address;
import dslabs.framework.Node;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
class ViewServer extends Node {
  static final int STARTUP_VIEWNUM = 0;
  private static final int INITIAL_VIEWNUM = 1;

  // Your code here...

  View view;
  Set<Address> servers;

  boolean primaryCurrent;

  /* -----------------------------------------------------------------------------------------------
   *  Construction and Initialization
   * ---------------------------------------------------------------------------------------------*/
  public ViewServer(Address address) {
    super(address);
  }

  @Override
  public void init() {
    set(new PingCheckTimer(), PING_CHECK_MILLIS);
    // Your code here...
    view = new View(STARTUP_VIEWNUM,null,null);
    servers = new HashSet<>();
    primaryCurrent = false;
  }

  /* -----------------------------------------------------------------------------------------------
   *  Message Handlers
   * ---------------------------------------------------------------------------------------------*/
  private void handlePing(Ping m, Address sender) {
    servers.add(sender);

    // View Server startup edge case - make sender primary
    if (view.viewNum() == STARTUP_VIEWNUM) {
      view = new View(INITIAL_VIEWNUM, sender, null);
      //System.out.println(view + "here3");
    }

    //System.out.println("entered: " + m.viewNum() + " - " + view.viewNum() + " - " + sender + " - " + view.primary() + "--" + (m.viewNum() == view.viewNum()) + "--" + (sender == view.primary()));
    // Primary is acking with current view
    if (m.viewNum() == view.viewNum() && sender.equals(view.primary())) {
      primaryCurrent = true;

      // special case: an idle server pinged before and can become backup --> initialized here
      if (view.backup() == null) {
        servers.remove(sender);
        Iterator<Address> iter = servers.iterator();
        if (iter.hasNext()) {
          view = new View(view.viewNum() + 1, sender, iter.next());
          primaryCurrent = false;
        }
        servers.add(sender);
      }
    }
    // Primary acked already and current view has no backup - make sender backup
      if (view.primary() != null && view.backup() == null && primaryCurrent && !sender.equals(view.primary())) {
      primaryCurrent = false;
      view = new View(view.viewNum() + 1, view.primary(), sender);
    }
    ViewReply result = new ViewReply(view);
    send(result, sender);
  }

  private void handleGetView(GetView m, Address sender) {
    // Your code here...
    ViewReply result = new ViewReply(view);
    send(result, sender);
  }

  /* -----------------------------------------------------------------------------------------------
   *  Timer Handlers
   * ---------------------------------------------------------------------------------------------*/
  private void onPingCheckTimer(PingCheckTimer t) {
    // Your code here...
    // Primary not in set of recently pinged servers and primaryCurrent is true: move to next view
    if (!servers.contains(view.primary()) && primaryCurrent && !servers.isEmpty()) {
      Address primary = view.backup();
      servers.remove(primary);
      Address backup = null;
      Iterator<Address> iter = servers.iterator();
      if (iter.hasNext()) {
        backup = iter.next();
      }

      view = new View(view.viewNum() + 1, primary, backup);
      primaryCurrent = false;
    }


    // Backup not in set of recently pinged servers and primaryCurrent is true: move to next view
    if (!servers.contains(view.backup()) && primaryCurrent) {
      Address backup = null;
      servers.remove(view.primary());
      Iterator<Address> iter = servers.iterator();
      if (iter.hasNext()) {
        backup = iter.next();
      }

      if (!(view.backup() == null && backup == null)) {
        view = new View(view.viewNum() + 1, view.primary(), backup);
        primaryCurrent = false;
      }
    }

    servers.clear();
    set(t, PING_CHECK_MILLIS);

  }

  /* -----------------------------------------------------------------------------------------------
   *  Utils
   * ---------------------------------------------------------------------------------------------*/
  // Your code here...

}