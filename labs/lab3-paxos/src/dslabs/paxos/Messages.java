package dslabs.paxos;

import dslabs.atmostonce.AMOCommand;
import dslabs.atmostonce.AMOResult;
import dslabs.framework.Address;
import dslabs.framework.Message;
import dslabs.paxos.PaxosServer.LogEntry;
import dslabs.paxos.PaxosServer.Pvalue;
import java.util.Map;
import lombok.Data;

// Your code here...
@Data
class PaxosRequest implements Message {
  private final AMOCommand command;
}

@Data
class PaxosReply implements Message {
  private final AMOResult result;
}

@Data
class HeartBeatResponse implements Message {
  private final int slot;
}

@Data
class Heartbeat implements Message {
  private final int garbage_slot;
  private final Map<Integer, LogEntry> log;
  private final int slot_in;
  private final int slot_out;
  private final Ballot ballot_leader;
}

@Data
class Ballot implements Message, Comparable<Ballot> {
  private final int roundNum;
  private final Address address;

  @Override
  public int compareTo(Ballot other) {
    int compare = Integer.compare(this.roundNum, other.roundNum);
    if (compare != 0) {
      return compare;
    }
    return this.address.compareTo(other.address);
  }
}

@Data
class P1a implements Message {
  private final Ballot ballot;
}

@Data
class P1b implements Message {
  private final Map<Integer, LogEntry> log;
  private final Ballot ballot;
}

@Data
class P2a implements Message {
  private final Pvalue pvalue;
}

@Data
class P2b implements Message {
  private final Ballot ballot;
  private final Pvalue slot;
}
