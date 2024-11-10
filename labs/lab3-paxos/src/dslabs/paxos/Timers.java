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

final class HeartbeatCheckTimer implements Timer {
  //static final int CLIENT_RETRY_MILLIS = 100;
}


final class HeartbeatSenderTimer implements Timer {
  //static final int CLIENT_RETRY_MILLIS = 100;
}

final class P1aTimer implements Timer {
  //static final int CLIENT_RETRY_MILLIS = 100;
}

final class P2aTimer implements Timer {
  //static final int CLIENT_RETRY_MILLIS = 100;
}