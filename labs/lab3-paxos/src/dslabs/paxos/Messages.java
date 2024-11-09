package dslabs.paxos;

import dslabs.framework.Address;
import dslabs.framework.Command;
import dslabs.framework.Message;
import java.util.List;
import lombok.Data;

// Your code here...
@Data
class HeartBeatResponse implements Message {
  private final int slot;
}

@Data
class Heartbeat implements Message {
  private final int garbage_slot;
  // leaders log
  private final int slot_in;
  private final int slot_out;
}

@Data
class Ballot implements Message {
  private final double roundNum;
  private final Address address;
}

@Data
class P1a implements Message {
  private final Ballot ballot;
}

@Data
class Pvalue implements Message {
  private final Ballot ballot;
  private final int slot;
  private final Command command;
}

@Data
class P1b implements Message {
  private final List<Pvalue> pvalues;
}

@Data
class P2a implements Message {
  private final Pvalue pvalue;
}

@Data
class P2b implements Message {
  private final Ballot ballot;
  private final int slot;
}