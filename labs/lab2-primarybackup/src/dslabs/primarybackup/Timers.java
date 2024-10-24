package dslabs.primarybackup;

import dslabs.framework.Timer;
import lombok.Data;

@Data
final class PingCheckTimer implements Timer {
  static final int PING_CHECK_MILLIS = 100;
}

@Data
final class PingTimer implements Timer {
  static final int PING_MILLIS = 25;
}

@Data
final class ClientTimer implements Timer {
  static final int CLIENT_RETRY_MILLIS = 25;

  // Your code here...
  private final int sequenceNum;
}

// Your code here...

@Data
final class StateTransferTimer implements Timer {
  static final int STATE_TRANSFER_RETRY_MILLIS = 100;
}

@Data
final class GetViewTimer implements Timer {
  static final int PING_MILLIS = 25;
}