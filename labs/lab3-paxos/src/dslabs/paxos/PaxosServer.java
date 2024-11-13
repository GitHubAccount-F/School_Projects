package dslabs.paxos;

import static dslabs.paxos.HeartbeatCheckTimer.HEARTBEATCHECK_RETRY_MILLIS;
import static dslabs.paxos.HeartbeatSenderTimer.HEARTBEATSENDER_RETRY_MILLIS;
import static dslabs.paxos.P1aTimer.P1aTimer_RETRY_MILLIS;
import static dslabs.paxos.P2aTimer.P2aTimer_RETRY_MILLIS;
import static dslabs.paxos.ClientTimer.CLIENT_RETRY_MILLIS;

import dslabs.atmostonce.AMOApplication;
import dslabs.framework.Address;
import dslabs.framework.Application;
import dslabs.framework.Command;
import dslabs.framework.Node;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class PaxosServer extends Node {
  /** All servers in the Paxos group, including this one. */
  private final Address[] servers;
  AMOApplication app;

  private int majority; // varible representing int size of majority
  // Your code here...
  // Replica state
  private Map<Integer, LogEntry> log;
  private int slot_out;
  private int slot_in;
  private Map<Address, Integer> latest_Executed_List;
  private int garbage_slot;


  // Acceptor state
  private Ballot ballot_acceptor;
  private List<Pvalue> accepted_Pvalues;
  private boolean receivedHeartbeat; // used to verify you received a heartbeat from leader
    //in between two-heartbeat timers

  // Leader state
  private boolean active;
  private Ballot ballot_leader;
  private Map<Pvalue, List<Address>> proposals;
  // for the chosen variable, I'm thinking proposals handle that
  // as we can keep track of if majority of servers accept a proposal inside the map
  private Map<Address, List<Pvalue>> seen; // used for election when trying to become leader

  /* -----------------------------------------------------------------------------------------------
   *  Construction and Initialization
   * ---------------------------------------------------------------------------------------------*/
  public PaxosServer(Address address, Address[] servers, Application app) {
    super(address);
    this.servers = servers;
    app = new AMOApplication<>(app, new HashMap<>(), new HashMap<>());

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
    if (this.address().equals(servers[0])) {
      // we hardcode the first server to be the leader
      active = true;
      ballot_leader = new Ballot(0, this.address());
      proposals = new HashMap<>();
      seen = new HashMap<>();
      // set acceptor state to null
      ballot_acceptor = new Ballot(0, this.address());
      accepted_Pvalues = null;
      receivedHeartbeat = false;
    } else { // for acceptors
      active = false;
      ballot_acceptor = new Ballot(-1, this.address()); // we set the round to -1 to it can accept ballots from leader
      accepted_Pvalues = new ArrayList<>();
      receivedHeartbeat = false;
      // set leader state to null
      ballot_leader = null;
      proposals = null;
      seen = null;
    }
    // set replica state for all servers
    log = new HashMap<>();
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
    // Your code here...
    if (garbage_slot >= logSlotNum) {
      return PaxosLogSlotStatus.CLEARED;
    }
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
    // if either it was garbage collected or we don't have that log slot
    if (garbage_slot >= logSlotNum || !log.containsKey(logSlotNum) ||
        status(logSlotNum) == PaxosLogSlotStatus.CLEARED ||
        status(logSlotNum) == PaxosLogSlotStatus.EMPTY) {
      return null;
    }
    LogEntry entry = log.get(logSlotNum);
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
    // Your code here...
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
    // finish implementing
    return 0;
  }

  /* -----------------------------------------------------------------------------------------------
   *  Message Handlers
   * ---------------------------------------------------------------------------------------------*/
  private void handlePaxosRequest(PaxosRequest m, Address sender) {
    // Your code here...
    if (active && !app.alreadyExecuted(m.command())) {
      int slotChosen = slot_in++;
      Pvalue pvalue = new Pvalue(this.ballot_leader, slotChosen, m.command());
      P2a message = new P2a(pvalue);
      for (int i = 0; i < servers.length; i++) {
        if (!this.address().equals(servers[i])) {
          send(message, servers[i]);
        }
      }
      set(new P2aTimer(pvalue), P2aTimer_RETRY_MILLIS);
    } else if(active && app.alreadyExecuted(m.command())) {
        send(new PaxosReply(app.execute(m.command())), sender);
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
    if (!active) { // acceptor
      if (this.ballot_acceptor.compareTo(m.ballot()) <= 0) {
        this.ballot_acceptor = m.ballot();
        P1b message = new P1b(accepted_Pvalues);
        send(message, sender);
      }
    } else { // leader
      if (this.ballot_leader.compareTo(m.ballot()) < 0) {
        // become follower
        active = false;
        proposals = null;
        seen = null;
        ballot_leader = null;
        ballot_acceptor = m.ballot();
        accepted_Pvalues = new ArrayList<>();
        P1b message = new P1b(accepted_Pvalues);
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

    if (!active && !accepted_Pvalues.contains(m.pvalue())) {
      if (this.ballot_acceptor.compareTo(m.pvalue().ballot()) <= 0 &&
          !log.containsKey(m.pvalue().slot()) && m.pvalue().slot() == slot_in) {
        // we accept
        slot_in++;
        LogEntry entry = new LogEntry(m.pvalue().ballot(), PaxosLogSlotStatus.ACCEPTED, m.pvalue().command());
        log.put(m.pvalue().slot(), entry);
        // send back p2B message
        P2b message = new P2b(m.pvalue().ballot(), m.pvalue());
        send(message, sender);
      }

    }

  }

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
    if (!active) {
      if (seen == null) {
        seen = new HashMap<>();
      }
      if (!seen.containsKey(sender)) {
        seen.put(sender, m.pvalues());
      }
      // check if we have a majority
      if (seen.keySet().size() >= majority) {
        active = true;
        proposals = new HashMap<>();
        // perform logic of picking proposals to send out
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
    if (active) {
      if (proposals.containsKey(m.slot())) { // verifies we haven't removed the proposal yet(there is not majoirty yet)
        List<Address> list = proposals.get(m.slot());
        list.add(sender);
        if (list.size() >= majority) {
          log.get(m.slot().slot()).setStatus(PaxosLogSlotStatus.CHOSEN);
          if (m.slot().slot() == slot_out) {
            slot_out++;
            // only execute command whenever we increment slot_out
            app.execute(m.slot().command());
            // remove proposal
            proposals.remove(m.slot());
          }
        } else {
          // wait for majority to occur
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

    if (!active) {
      // verify if heartbeat is from leader
      if (m.ballot_leader().equals(this.ballot_acceptor)) {
        receivedHeartbeat = true;
        // update acceptor logs
        // execute commands
        // perform garbage collection
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
      this.ballot_acceptor = new Ballot(this.ballot_acceptor.roundNum() + 1, this.address());
      P1a message = new P1a(ballot_acceptor);
      for (int i = 0; i < servers.length; i++) {
        if (this.address() != servers[i]) {
          send(message, servers[i]);
        }
      }
      this.set(new P1aTimer(message), P1aTimer_RETRY_MILLIS);
    } else if (receivedHeartbeat && !active) {
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
      if (latest_Executed_List.size() == servers.length) {
        for (int i = 0; i < latest_Executed_List.size(); i++) {
          garbage_slot = Math.min(latest_Executed_List.get(i), garbage_slot);
        }
      }
      Heartbeat beat = new Heartbeat(garbage_slot, log, slot_in, slot_out, ballot_leader);
      for (int i = 0; i < servers.length; i++) {
        if (this.address() != servers[i]) {
          send(beat, servers[i]);
        }
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
        if (this.address() != servers[i]) {
          send(t.p1a(), servers[i]);
        }
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
    if (active && proposals.get(t.slot()).size() < majority) {
      for (int i = 0; i < servers.length; i++) {
        send(t.slot(), servers[i]);
      }
      this.set(t, P2aTimer_RETRY_MILLIS);
    }
  }

  /* -----------------------------------------------------------------------------------------------
   *  Utils
   * ---------------------------------------------------------------------------------------------*/
  // Your code here...
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

  public static int findMinSlot(Map<Address, Integer> latest_Executed_List) {
    int min_slot = Integer.MAX_VALUE;
    for (Address x : latest_Executed_List.keySet()) {
      min_slot = Math.min(latest_Executed_List.get(x), min_slot);
    }
    return min_slot;
  }
}

