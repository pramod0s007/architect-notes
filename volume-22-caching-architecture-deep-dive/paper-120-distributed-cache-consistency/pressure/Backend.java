import java.util.HashMap;
import java.util.Map;

/**
 * The single source of truth every cache node reads from and writes through to.
 * A real system would be a database or a remote service; here it's an in-memory
 * map so the demo stays deterministic and dependency-free.
 */
public class Backend {
    private final Map<String, String> store = new HashMap<>();

    public void write(String key, String value) {
        store.put(key, value);
    }

    public String read(String key) {
        return store.get(key);
    }
}
