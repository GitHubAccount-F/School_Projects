package dslabs.kvstore;

import dslabs.atmostonce.AMOCommand;
import dslabs.framework.Application;
import dslabs.framework.Command;
import dslabs.framework.Result;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class KVStore implements Application {

  public interface KVStoreCommand extends Command {}

  public interface SingleKeyCommand extends KVStoreCommand {
    String key();
  }

  @Data
  public static final class Get implements SingleKeyCommand {
    @NonNull private final String key;

    @Override
    public boolean readOnly() {
      return true;
    }
  }

  @Data
  public static final class Put implements SingleKeyCommand {
    @NonNull private final String key, value;
  }

  @Data
  public static final class Append implements SingleKeyCommand {
    @NonNull private final String key, value;
  }

  public interface KVStoreResult extends Result {}

  @Data
  public static final class GetResult implements KVStoreResult {
    @NonNull private final String value;
  }

  @Data
  public static final class KeyNotFound implements KVStoreResult {}

  @Data
  public static final class PutOk implements KVStoreResult {}

  @Data
  public static final class AppendResult implements KVStoreResult {
    @NonNull private final String value;
  }

  // Your code here...
  Map<String, String> storage = new HashMap<>();

  @Override
  public KVStoreResult execute(Command command) {
    if (command instanceof Get) {
      Get g = (Get) command;
      // Your code here...
      if (!storage.containsKey(g.key)) {
        return new KeyNotFound();
      }
      return new GetResult(storage.get(g.key));
    }

    if (command instanceof Put) {
      Put p = (Put) command;
      // Your code here...
      storage.put(p.key, p.value);
      return new PutOk();

    }

    if (command instanceof Append) {
      Append a = (Append) command;
      // Your code here...
      if (!storage.containsKey(a.key())) {
        storage.put(a.key, "");
      }
      storage.put(a.key, storage.get(a.key) + a.value);
      return new AppendResult(storage.get(a.key));


    }
    throw new IllegalArgumentException();
  }
}
