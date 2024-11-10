package dslabs.paxos;

import static dslabs.paxos.HeartbeatCheckTimer.HEARTBEATCHECK_RETRY_MILLIS;
import static dslabs.paxos.HeartbeatSenderTimer.HEARTBEATSENDER_RETRY_MILLIS;
import static dslabs.paxos.P1aTimer.P1aTimer_RETRY_MILLIS;
import static dslabs.paxos.P2aTimer.P2aTimer_RETRY_MILLIS;
import static dslabs.paxos.ClientTimer.CLIENT_RETRY_MILLIS;

import dslabs.framework.Address;
import dslabs.framework.Application;
import dslabs.framework.Command;
import dslabs.framework.Node;
import dslabs.primarybackup.GetViewTimer;
import java.util.List;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class PaxosServer extends Node {
  /** All servers in the Paxos group, including this one. */
  private final Address[] servers;

  private int majority; // varible representing int size of majority
  // Your code here...
  // Replica state
  private Map<Integer, LogEntry> log;
  private int slot_out;
  private int slot_in;
  private List<Integer> latest_Executed_List;


  // Acceptor state
  private Ballot ballot;
  private List<Pvalue> accepted_Pvalues;
  private boolean receivedHeartbeat; // used to verify you received a heartbeat from leader
    //in between two-heartbeat timers

  // Leader state
  private boolean active;
  private Ballot ballot_num;
  private Map<Pvalue, List<Address>> proposals;
  // for the chosen variable, I'm thinking proposals handle that
  // as we can keep track of if majority of servers accept a proposal inside the map
  private Map<Address, List<Pvalue>> seen; // used for election when trying to become leader






  class LogEntry {

    private Ballot ballot;
    private PaxosLogSlotStatus status;
    private Command command;

    LogEntry(Ballot ballot, PaxosLogSlotStatus status, Command command) {
      this.ballot = ballot;
      this.status = status;
      this.command = command;
    }

    public Ballot getLogBallot() {
      return ballot;
    }

    public PaxosLogSlotStatus getLogStatus() {
      return status;
    }

    public Command getLogCommand() {
      return command;
    }
  }
  /* -----------------------------------------------------------------------------------------------
   *  Construction and Initialization
   * ---------------------------------------------------------------------------------------------*/
  public PaxosServer(Address address, Address[] servers, Application app) {
    super(address);
    this.servers = servers;

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
    return null;
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
    // Your code here...
    return null;
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
    return 1;
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
    return 0;
  }

  /* -----------------------------------------------------------------------------------------------
   *  Message Handlers
   * ---------------------------------------------------------------------------------------------*/
  private void handlePaxosRequest(PaxosRequest m, Address sender) {
    // Your code here...
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
  }

  private void handleP2a(P2a m, Address sender) {
    // Your code here...
    /*
    // ***** case idk about: what if we get a p2a message from another leader

      if acceptor and has not already accepted this P2a:
        only accept if the ballot number in the P2a is >= to current ballot and slot is free
        send back P2b message
     */

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
  }

  private void handleHeartbeatResponse(HeartBeatResponse m, Address sender) {
    /*
    if you are the leader(active = true):
      add latest slot executed by servers to LatestExecutedList
      if LatestExecutedList has all servers(include leader)
        update garbage_collect
     */
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
      this.ballot = new Ballot(this.ballot.roundNum() + 1, this.address());
      P1a message = new P1a(ballot);
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
      int garbage_slot = -1;
      if (latest_Executed_List.size() == servers.length) {
        for (int i = 0; i < latest_Executed_List.size(); i++) {
          garbage_slot = Math.min(latest_Executed_List.get(i), garbage_slot);
        }
      }
      Heartbeat beat = new Heartbeat(garbage_slot, log, slot_in, slot_out);
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
}
