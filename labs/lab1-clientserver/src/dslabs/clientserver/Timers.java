package dslabs.clientserver;

import dslabs.framework.Timer;
import dslabs.pingpong.PingApplication.Ping;
import lombok.Data;

@Data
final class ClientTimer implements Timer {
  static final int CLIENT_RETRY_MILLIS = 100;

  // Your code here...
  private final Request request;

}
