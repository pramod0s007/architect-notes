import java.util.HashMap;
import java.util.Map;

/**
 * A read replica that only reflects writes once they have actually
 * been applied from the replication stream -- its data, and its
 * "applied version," can genuinely lag behind the primary's true
 * state for a while.
 */
public class Replica {
    private final Map<String, String> data = new HashMap<>();
    private int appliedVersion = 0;

    public void apply(ReplicationLog.Entry entry) {
        data.put(entry.key, entry.value);
        appliedVersion = entry.version;
    }

    public String read(String key) {
        return data.get(key);
    }

    public int getAppliedVersion() {
        return appliedVersion;
    }
}
