package dslabs.paxos;

import static dslabs.paxos.HeartbeatCheckTimer.HEARTBEATCHECK_RETRY_MILLIS;
import static dslabs.paxos.HeartbeatSenderTimer.HEARTBEATSENDER_RETRY_MILLIS;
import static dslabs.paxos.P1aTimer.P1aTimer_RETRY_MILLIS;
import static dslabs.paxos.P2aTimer.P2aTimer_RETRY_MILLIS;
import static dslabs.paxos.ClientTimer.CLIENT_RETRY_MILLIS;

import dslabs.atmostonce.AMOApplication;
import dslabs.atmostonce.AMOCommand;
import dslabs.framework.Address;
import dslabs.framework.Application;
import dslabs.framework.Command;
import dslabs.framework.Message;
import dslabs.framework.Node;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class PaxosServer extends Node {
  /** All servers in the Paxos group, including this one. */
  private final Address[] servers;
  AMOApplication app;

  private int majority; // variable representing int size of majority
  // Your code here...
  // Replica state
  private Map<Integer, LogEntry> log; // we will only store chosen commands here
  private int slot_out; // location of command that hasn't been
  private int slot_in; // where to put new commands
  private Map<Address, Integer> latest_Executed_List;
  private int garbage_slot;
  private Ballot ballot;


  // Acceptor state
  //private Ballot ballot_acceptor;
  //private Map<Integer, Pvalue> accepted_Pvalues; // we will only store accepted commands here
  //private List<Pvalue> accepted_Pvalues;
  private boolean receivedHeartbeat; // used to verify you received a heartbeat from leader
    //in between two-heartbeat timers

  // Leader state
  private boolean active;
  //private Ballot ballot_leader;
  private Map<Pvalue, List<Address>> proposals;
  // for the chosen variable, I'm thinking proposals handle that
  // as we can keep track of if majority of servers accept a proposal inside the map
  private Map<Address,  Map<Integer, LogEntry>> seen; // used for election when trying to become leader

  /* -----------------------------------------------------------------------------------------------
   *  Construction and Initialization
   * ---------------------------------------------------------------------------------------------*/
  public PaxosServer(Address address, Address[] servers, Application app) {
    super(address);
    this.servers = servers;
    this.app = new AMOApplication<>(app, new HashMap<>(), new HashMap<>());

    // Your code here...
    // initialize all variables
  }

  @Override
  public void init() {
    // Your code here...
    // if this.address == server0(or server1 depending on which is the first server)
      /*
      we set active to true,
       */
    // set heartbeatCheckTimer
    majority = (servers.length / 2) + 1;
    if (this.address().equals(servers[0])) {
      // we hardcode the first server to be the leader
      active = true;
      proposals = new HashMap<>();
      seen = new HashMap<>();
      ballot = new Ballot(0, this.address());
      // set acceptor state to null
      //accepted_Pvalues = null;
      receivedHeartbeat = false;
    } else { // for acceptors
      active = false;
      //accepted_Pvalues = new HashMap<>();
      receivedHeartbeat = false;
      // set leader state to null
      proposals = null;
      seen = null;
      ballot = new Ballot(-1, this.address());
    }
    // set replica state for all servers

    log = new TreeMap<>();
    slot_out = 1;
    slot_in = 1;
    latest_Executed_List = new HashMap<>();
    garbage_slot = 0; // slots start at 1, so it should be true forever that
    // all slots >= garbage_slot + 1 have not been garbage collected
  }

  /* -----------------------------------------------------------------------------------------------
   *  Interface Methods
   *
   *  Be sure to implement the following methods correctly. The test code uses them to check
   *  correctness more efficiently.
   * ---------------------------------------------------------------------------------------------*/

  /**
   * Return the status of a given slot in the server's local log.
   *
   * <p>If this server has garbage-collected this slot, it should return {@link
   * PaxosLogSlotStatus#CLEARED} even if it has previously accepted or chosen command for this slot.
   * If this server has both accepted and chosen a command for this slot, it should return {@link
   * PaxosLogSlotStatus#CHOSEN}.
   *
   * <p>Log slots are numbered starting with 1.
   *
   * @param logSlotNum the index of the log slot
   * @return the slot's status
   * @see PaxosLogSlotStatus
   */
  public PaxosLogSlotStatus status(int logSlotNum) {
    //System.out.println("Server = " + this.address() + "  \nstatus = " + logSlotNum);
    // Your code here...
    if (garbage_slot < logSlotNum && !this.log.containsKey(logSlotNum)) {
      return PaxosLogSlotStatus.EMPTY;
    }
    if (garbage_slot >= logSlotNum && !this.log.containsKey(logSlotNum)) {
      return PaxosLogSlotStatus.CLEARED;
    }
    System.out.println("Server = " + this.address() + "  \n   slot = " + logSlotNum + " \n   status = " + log.get(logSlotNum).status());
    return log.get(logSlotNum).status();
  }

  /**
   * Return the command associated with a given slot in the server's local log.
   *
   * <p>If the slot has status {@link PaxosLogSlotStatus#CLEARED} or {@link
   * PaxosLogSlotStatus#EMPTY}, this method should return {@code null}. Otherwise, return the
   * command this server has chosen or accepted, according to {@link PaxosServer#status}.
   *
   * <p>If clients wrapped commands in {@link dslabs.atmostonce.AMOCommand}, this method should
   * unwrap them before returning.
   *
   * <p>Log slots are numbered starting with 1.
   *
   * @param logSlotNum the index of the log slot
   * @return the slot's contents or {@code null}
   * @see PaxosLogSlotStatus
   */
  public Command command(int logSlotNum) {
    //System.out.println("Server = " + this.address() + "   \ncommand = " + logSlotNum);
    // if either it was garbage collected or we don't have that log slot
    if (garbage_slot >= logSlotNum || !log.containsKey(logSlotNum) ||
        status(logSlotNum) == PaxosLogSlotStatus.CLEARED ||
        status(logSlotNum) == PaxosLogSlotStatus.EMPTY) {
      return null;
    }
    System.out.println("Server = " + this.address() + "  \n   slot = " + logSlotNum + " \n   command = " + log.get(logSlotNum));
    LogEntry entry = log.get(logSlotNum);
    if (entry.command() instanceof AMOCommand) {
      return ((AMOCommand) entry.command()).command();
    }
    return entry.command();
  }

  /**
   * Return the index of the first non-cleared slot in the server's local log. The first non-cleared
   * slot is the first slot which has not yet been garbage-collected. By default, the first
   * non-cleared slot is 1.
   *
   * <p>Log slots are numbered starting with 1.
   *
   * @return the index in the log
   * @see PaxosLogSlotStatus
   */

  public int firstNonCleared() {
    return garbage_slot + 1;
  }

  /**
   * Return the index of the last non-empty slot in the server's local log, according to the defined
   * states in {@link PaxosLogSlotStatus}. If there are no non-empty slots in the log, this method
   * should return 0.
   *
   * <p>Log slots are numbered starting with 1.
   *
   * @return the index in the log
   * @see PaxosLogSlotStatus
   */
  public int lastNonEmpty() {
    // Your code here...
    // Iterate backwards from the last slot number
    for (int i = slot_in - 1; i > garbage_slot; i--) {
      if (log.get(i).status != PaxosLogSlotStatus.EMPTY) {
        return i;
      }
    }
    // If all slots are empty, return 0
    return 0;
  }

  /* -----------------------------------------------------------------------------------------------
   *  Message Handlers
   * ---------------------------------------------------------------------------------------------*/
  private void handlePaxosRequest(PaxosRequest m, Address sender) {
    // Your code here...
    // makes sure server is set up
    System.out.println("Server = " + this.address() + "   \n    handlePaxosRequest = " + m);
    System.out.println("Server = " + this.address() + " is leader = " + active + " and app = " + (this.app == null));
    if (this.app != null) {
     // System.out.println("here1");
      // we must be the leader to accept
      // if not already executed, send to all servers
      if (active && !app.alreadyExecuted(m.command())) {
       // System.out.println("here2");
        int slotChosen = slot_in;
        Pvalue pvalue = new Pvalue(this.ballot, slotChosen, m.command());
        if (!this.proposals.containsKey(pvalue) && !this.proposals.containsKey(new Pvalue(this.ballot, slotChosen - 1, m.command()))) {
          slot_in++;
          proposals.put(pvalue, new ArrayList<>());
          P2a message = new P2a(pvalue);
          for (int i = 0; i < servers.length; i++) {
            send(message, servers[i]);
          }
          // set timer for this p2a message
          set(new P2aTimer(message), P2aTimer_RETRY_MILLIS);
        }

      } else if(active && app.alreadyExecuted(m.command())) {
        // if we already executed, just send something back to client
        send(new PaxosReply(app.execute(m.command())), sender);
      }
    }

    /*
      accept only if you are the leader and that application has not executed it
        find available slot in replica, increment slot_in
        send out proposals to acceptors(proposals are pvalues, I already defined them in messages)
        wrap proposal with a P2a message, and send to all servers(including leader)
        set p2a timer
       if app executed command,
        send back execute(command) from app
     */

  }

  private void handleP1a(P1a m, Address sender) {
    // Your code here...
    /*
        if you are a leader and receive a higher ballot:
          become acceptor/follower
          (maybe) send p1b messages
          set seen, proposals to null
        else if you are an acceptor:
          compare to current ballot,
            if higher update ballot and send p1b message
     */
    System.out.println("Server = " + this.address() + "   \nhandleP1a = " + m);
    if (!active) { // acceptor
      if (this.ballot.compareTo(m.ballot()) <= 0) {
        this.ballot = m.ballot();
        P1b message = new P1b(log);
        send(message, sender);
      }
    } else { // leader
      if (this.ballot.compareTo(m.ballot()) < 0) {
        // become acceptor
        active = false;
        proposals = null;
        seen = null;
        this.ballot = m.ballot();
        P1b message = new P1b(log);
        send(message, sender);
      }
    }
  }



  private void handleP2a(P2a m, Address sender) {
    // Your code here...
    /*
    // ***** case idk about: what if we get a p2a message from another leader
    // i assume we become a follower

      if acceptor and has not already accepted this P2a:
        only accept if the ballot number in the P2a is >= to current ballot and slot is free
        send back P2b message
     */
// // only accept p2a message if ballot is <= ballot in message
    System.out.println("Server = " + this.address() + "  ballot = " + this.ballot + "  \n  handleP2a = " + m);
    if (this.ballot.compareTo(m.pvalue().ballot()) <= 0) {
      if (active) { // handle case if a leader receives a p2a from a higher-ballot leader
        if (this.ballot.compareTo(m.pvalue().ballot()) < 0) {
          active = false;
          seen = null;
          proposals = null;
        }
      }
      this.ballot = m.pvalue().ballot();
      // ifs its already chosen, ignore it
      System.out.println("      Server = " + this.address() + "  handleP2a = here 0");
      if (log.containsKey(m.pvalue().slot()) &&
          (log.get(m.pvalue().slot()).status == PaxosLogSlotStatus.ACCEPTED)) {
        System.out.println("      Server = " + this.address() + "  handleP2a = here 1");
        // only update if the ballot is higher than the current one in the log
        if (this.ballot.compareTo(m.pvalue().ballot()) < 0) {
          LogEntry entry = new LogEntry(m.pvalue().ballot(), PaxosLogSlotStatus.ACCEPTED, m.pvalue().command());
          log.put(m.pvalue().slot(), entry);
          P2b message = new P2b(m.pvalue().ballot(), m.pvalue());
          send(message, sender);
        }
      } else if (!log.containsKey(m.pvalue().slot())) { // if the slot is new, it must go to slot_in
        System.out.println("       Server = " + this.address() + " handleP2a = here 2");
        // below: for the leader, we already incremented slot_in when sending the p2a, but we didn't do this for other acceptors
        if ((active && m.pvalue().slot + 1 == slot_in) || (!active && m.pvalue().slot() == slot_in)) {
          if (m.pvalue().slot() == slot_in) {
            slot_in++;
          }
          LogEntry entry = new LogEntry(m.pvalue().ballot(), PaxosLogSlotStatus.ACCEPTED, m.pvalue().command());
          log.put(m.pvalue().slot(), entry);
          P2b message = new P2b(m.pvalue().ballot(), m.pvalue());
          send(message, sender);
        }
      }

    }

  }

  // fix
  private void handleP1b(P1b m, Address sender) {
    // Your code here...
    /*
      if check if  # keys in "seen" is majority):
        set active to true
        iterate over map:
        for slots, if there is a majority for a command, then consider that chosen
        otherwise, add the highest ballot proposal into the proposal variable
     */
    // what if you become a leader and more p1b messages come in?
    System.out.println("Server = " + this.address() + "    \n handleP1b = " + m);
    if (!active) {
      if (seen == null) {
        seen = new HashMap<>(); // node for implementation, might set all "seen" to be empty map
        proposals = new HashMap<>();
      }
      if (!seen.containsKey(sender)) {
        seen.put(sender, log);
      }
      // check if we have a majority
      if (seen.keySet().size() >= majority) {
        active = true;
        proposals = findProposals(seen, this.ballot);
        // send out all proposals
        for (Pvalue x : proposals.keySet()) {
          for (int i = 0; i < servers.length; i++) {
            P2a message = new P2a(x);
            send(message, servers[i]);
          }
        }

      }
    }

  }

  private void handleP2b(P2b m, Address sender) {
    // Your code here...

    /*
      if you are the leader:
        accept p2b message inside the proposals map
        if there is a majority:
          increment slot_out(also update status of slot)
          execute command
          remove proposal from proposal map
        else:
          wait until you receive a majority
     */
    System.out.println("Server = " + this.address() +  "    \nhandleP2b = " + m);
    if (active) {
      if (proposals.containsKey(m.slot()) && !proposals.get(m.slot()).contains(sender)) { // verifies we haven't removed the proposal yet(there is not majoirty yet)
        System.out.println("Server = " + this.address() +  "  handleP2b here1 ");
        proposals.get(m.slot()).add(sender);
        if (proposals.get(m.slot()).size() >= majority) {
          System.out.println("Server = " + this.address() +  "  handleP2b here2 ");
          log.get(m.slot().slot()).setStatus(PaxosLogSlotStatus.CHOSEN);
          if (m.slot().slot() == slot_out) {
            slot_out = updateSlotOut(slot_out, slot_in, log);
            // only execute command whenever we increment slot_out
            app.execute(m.slot().command());
            // remove proposal
            proposals.remove(m.slot());
          }
        }
      }

    }
  }

  private void handleHeartbeat(Heartbeat m, Address sender) {
    /*
    case idk about: what if you are another leader that receives a heartbeat message
        if you are acceptor
          set receivedHeartbeat = true;
          update your log to match that of the leader(details on doc)
          update commands
          for garbage collection, if garbage_slot ! -1:
            remove all slots up to garbage_slot in replica(and in accepted list too???)
     */
    System.out.println("Server = " + this.address() + "    \n handleHeartbeat = " + m);
    if (m.ballot_leader().equals(this.ballot)) {
      receivedHeartbeat = true;
      // update logs
      this.slot_in = Math.max(this.slot_in, m.slot_in());
      for (int slot : m.log().keySet()) {
        if (!this.log.containsKey(slot)) {
          this.log.put(slot, log.get(slot));
        }
        if (m.log().get(slot).status == PaxosLogSlotStatus.CHOSEN) {
          this.log.get(slot).status(PaxosLogSlotStatus.CHOSEN);
          if (slot <= this.slot_out) {
            if (log.get(slot).command != null) {
              app.execute(log.get(slot).command);
            }
            if (slot == this.slot_out) {
              slot_out = slot_in;
            }
          }
        }
        if (m.log().get(slot).status == PaxosLogSlotStatus.ACCEPTED) {
          this.slot_out = slot;
        }
      }
      // execute commands

      // perform garbage collection
      if (garbage_slot != 0) {
        for (int slot : this.log.keySet()) {
          if (slot <= garbage_slot) {
            log.remove(slot);
          }
        }
      }
    }
  }

  private void handleHeartbeatResponse(HeartBeatResponse m, Address sender) {
    /*
    if you are the leader(active = true):
      add latest slot executed by servers to LatestExecutedList
      if LatestExecutedList has all servers(include leader)
        update garbage_collect
     */
    if (active) {
      latest_Executed_List.put(sender, m.slot());
      if (latest_Executed_List.keySet().size() == servers.length) {
        garbage_slot = findMinSlot(latest_Executed_List);
      }
    }
  }



  // Your code here...

  /* -----------------------------------------------------------------------------------------------
   *  Timer Handlers
   * ---------------------------------------------------------------------------------------------*/
  // Your code here...

  private void onHeartbeatCheckTimer(HeartbeatCheckTimer t) {
    /*
          if active = false and receivedHeartbeat = false
            server tries to become leader
            start sending p1a requests, but with a ballot consisting of
            (server number.round # + 1)
           else: <- this may need some additional checks to ensure no bugs slip past
             we already received a heartbeat, so set heartbeat to false
             reset timer

     */
    if (!active && !receivedHeartbeat) {
      // server did not receive ping in-between two consecutive timers, so
      // it tries to become leader
      seen = null;
      this.ballot = new Ballot(this.ballot.roundNum() + 1, this.address());
      P1a message = new P1a(ballot);
      for (int i = 0; i < servers.length; i++) {
        if (this.address() != servers[i]) {
          send(message, servers[i]);
        }
      }
      this.set(new P1aTimer(message), P1aTimer_RETRY_MILLIS);
    }
    if (receivedHeartbeat) {
      receivedHeartbeat = false;
      set(t, HEARTBEATCHECK_RETRY_MILLIS);
    }
  }

  private void onHeartbeatSenderTimer(HeartbeatSenderTimer t) {
  /*
    if you are the leader:
      send a heartbeat to all servers(except yourself) consisting of
      the latest slot to be executed, your log, garbage collect slot, slot_in, and slot_out
      reset timer

   */
    if (active) {
      Heartbeat beat = new Heartbeat(garbage_slot, log, slot_in, slot_out, ballot);
      for (int i = 0; i < servers.length; i++) {
        send(beat, servers[i]);
      }
      set(t, HEARTBEATSENDER_RETRY_MILLIS);
    }
  }

  private void onP1aTimer(P1aTimer t) {

    /*
        if you have not acknowledged another leader and still haven't received a majority of p1b
          send p1a again
          reset timer
     */
    if (!active && !receivedHeartbeat & seen.keySet().size() < majority) {
      for (int i = 0; i < servers.length; i++) {
        send(t.p1a(), servers[i]);
      }
      set(t, P1aTimer_RETRY_MILLIS);
    }
  }

  private void onP2aTimer(P2aTimer t) {
  /*
    if leader:
      if proposal has not received a majority yet, resend that proposals
      reset timer for that specific proposal.
   */
    //System.out.println("Server = " + this.address() + " onP2aTimer = " + t);
    if (active && proposals.containsKey(t.message().pvalue()) && proposals.get(t.message().pvalue()).size() < majority) {
      for (int i = 0; i < servers.length; i++) {
        send(t.message(), servers[i]);
      }
      this.set(t, P2aTimer_RETRY_MILLIS);
    }
  }

  /* -----------------------------------------------------------------------------------------------
   *  Utils
   * ---------------------------------------------------------------------------------------------*/
  // Your code here...


  @Data
  public static class Pvalue {
    private final Ballot ballot;
    private final int slot;
    private final Command command;

    public Pvalue(Ballot ballot, int slot, Command command) {
      this.ballot = ballot;
      this.slot = slot;
      this.command = command;
    }
  }

  @Data
  public static class LogEntry {
    private Ballot ballot;
    private PaxosLogSlotStatus status;
    private Command command;

    public LogEntry(Ballot ballot, PaxosLogSlotStatus status, Command command) {
      this.ballot = ballot;
      this.status = status;
      this.command = command;
    }

    public void setStatus(PaxosLogSlotStatus status) {
      this.status = status;
    }
  }

  // used to for garbage collection
  public static int findMinSlot(Map<Address, Integer> latest_Executed_List) {
    int min_slot = Integer.MAX_VALUE;
    for (Address x : latest_Executed_List.keySet()) {
      min_slot = Math.min(latest_Executed_List.get(x), min_slot);
    }
    return min_slot;
  }


  // Given the seen list, we try to create a new proposals list when a server was just elected leader
  public static Map<Pvalue, List<Address>> findProposals(Map<Address, Map<Integer, LogEntry>> seen, Ballot server) {
    int majority = (seen.keySet().size() / 2) + 1;
    int minimum = Integer.MAX_VALUE; // used to find any empty slots
    int maximum = Integer.MIN_VALUE;
    // This is what we will return
    Map<Pvalue, List<Address>> result = new HashMap<>();
    // IDK about this, but if we come across a slot that already has a CHOSEN, we can ignore that
    // slot in proposals
    Set<Integer> slotsToIgnore = new HashSet<>();
    // For each slot, we have a map for Ballots -> # we seen so far(i can probbably simplify this)
    Map<Integer, Map<Ballot, Integer>> storage = new HashMap<>();
    // Iterate through "seen"
    for (Address x : seen.keySet()) {
      // iterate through acceptor's log
      Map<Integer, LogEntry> log = seen.get(x);
      for (int slot: seen.get(x).keySet()) {
        minimum = Math.min(minimum, slot);
        maximum = Math.max(maximum, slot);
        // if slot is CHOSEN, ignore in proposals
        if (log.get(slot).status == PaxosLogSlotStatus.CHOSEN) {
          slotsToIgnore.add(slot);
        } else if (log.get(slot).status == PaxosLogSlotStatus.ACCEPTED) {
          if (!storage.containsKey(slot)) {
            storage.put(slot, new HashMap<>());
          }
          Ballot ballot = log.get(slot).ballot;
          storage.get(slot).put(ballot, storage.get(slot).getOrDefault(ballot, 0) + 1);
        }
      }
    }

    // Iterate through storage and find proposals
    for (int i = minimum; i <= maximum; i++) {
      if(!slotsToIgnore.contains(i)) {
        // for each slot, we will iterate thorugh it's map and find either the ballot
        // with the majority, or find the highest ballot and repropose that
        Map<Ballot, Integer> map = storage.get(i);
        Ballot highest = null;
        for (Ballot b : map.keySet()) {
          if (highest == null) {
            highest = b;
          } else if (map.get(b) == majority) {
            // if we detect a majority, we know this log slot was already chosen,
            // so we can ignore it
            /*
            LogEntry check = seen.get(highest.address()).get(i);
            result.put(new Pvalue(server, i, check.command), new ArrayList<>());
            */
            break;
          } else {
            if (b.compareTo(highest) > 0) {
              highest = b;
            }
          }
        }
        // if we found no majority ballot, repropose the highest slot
        LogEntry check = seen.get(highest.address()).get(i);
        result.put(new Pvalue(server, i, check.command), new ArrayList<>());
      } else if (!storage.containsKey(i)) {
        // propose No-op here
        result.put(new Pvalue(server, i, null), new ArrayList<>());
      }
    }
    return result;
  }

  // used to update slot_out whenever a command gets chosen
  public static int updateSlotOut(int slot_out, int slot_in, Map<Integer, LogEntry> log) {
    for (int i = slot_out + 1; i < slot_in; i++) {
      if (log.containsKey(i) && log.get(i).status == PaxosLogSlotStatus.ACCEPTED) {
        return i;
      }
    }
    return slot_in;
  }
}

