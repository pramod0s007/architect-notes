import java.util.HashMap;
import java.util.Map;

/**
 * The single source of truth for writes. Every write is assigned a
 * strictly increasing version number (like a database's log sequence
 * number) that downstream replicas can use to reason about how far
 * behind they are.
 */
public class Primary {
    private final Map<String, String> data = new HashMap<>();
    private int currentVersion = 0;

    public int write(String key, String value) {
        currentVersion++;
        data.put(key, value);
        return currentVersion;
    }

    public String read(String key) {
        return data.get(key);
    }

    public int getCurrentVersion() {
        return currentVersion;
    }
}
