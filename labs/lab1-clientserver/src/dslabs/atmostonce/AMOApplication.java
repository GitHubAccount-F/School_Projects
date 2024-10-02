package dslabs.atmostonce;

import dslabs.framework.Address;
import dslabs.framework.Application;
import dslabs.framework.Command;
import dslabs.framework.Result;
import dslabs.kvstore.KVStore;
import dslabs.kvstore.KVStore.KVStoreResult;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@EqualsAndHashCode
@ToString
@RequiredArgsConstructor
public final class AMOApplication<T extends Application> implements Application {
  @Getter @NonNull private final T application;

  // Your code here...
  //@NonNull final private KVStore kvstore;
  @NonNull final private Map<Address, AMOResult> pastRequests;
  @NonNull final private Map<Address, Integer> checkPastRequests;

  @Override
  public AMOResult execute(Command command) {
    if (!(command instanceof AMOCommand)) {
      throw new IllegalArgumentException();
    }

    AMOCommand amoCommand = (AMOCommand) command;

    // tests to see if command was not already executed
    if (alreadyExecuted(amoCommand)) {
      return pastRequests.get(amoCommand.clientAddress());
    } else {
      // execute command
      Result res = application.execute(amoCommand.command());

      AMOResult result = new AMOResult(res, amoCommand.sequenceNum(), amoCommand.clientAddress());
      // store results in case client tries same request
      pastRequests.put(result.clientAddress(), result);
      checkPastRequests.put(result.clientAddress(), result.sequenceNum());

      return result;
    }

    // Your code here...
  }

  public Result executeReadOnly(Command command) {
    if (!command.readOnly()) {
      throw new IllegalArgumentException();
    }

    if (command instanceof AMOCommand) {
      return execute(command);
    }

    return application.execute(command);
  }

  public boolean alreadyExecuted(AMOCommand amoCommand) {
    // Your code here...
    return checkPastRequests.containsKey(amoCommand.clientAddress()) &&
        (checkPastRequests.get(amoCommand.clientAddress()) >= amoCommand.sequenceNum());
  }
}
