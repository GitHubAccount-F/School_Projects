package dslabs.paxos;

import dslabs.framework.Timer;
import lombok.Data;

@Data
final class ClientTimer implements Timer {
  static final int CLIENT_RETRY_MILLIS = 100;
  private final int sequenceNumber;
  // Your code here...
}

// Your code here...
@Data
final class HeartbeatCheckTimer implements Timer {
  static final int HEARTBEATCHECK_RETRY_MILLIS = 110;
}

@Data
final class HeartbeatSenderTimer implements Timer {
  static final int HEARTBEATSENDER_RETRY_MILLIS = 50;
}

@Data
final class P1aTimer implements Timer {
  static final int P1aTimer_RETRY_MILLIS = 45;
  private final P1a p1a;
}

@Data
final class P2aTimer implements Timer {
  static final int P2aTimer_RETRY_MILLIS = 25;
  private final P2a message;
}
