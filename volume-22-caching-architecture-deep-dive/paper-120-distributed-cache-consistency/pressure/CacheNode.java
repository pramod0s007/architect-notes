import java.util.HashMap;
import java.util.Map;

/**
 * A cache node with NO invalidation mechanism at all: once a value is cached
 * on a miss, it is served forever, even after the backend value changes.
 * This models a naive per-instance local cache in a multi-node deployment
 * where a write on one node has no way to reach the others.
 */
public class CacheNode {
    private final String name;
    private final Backend backend;
    private final Map<String, String> cache = new HashMap<>();

    public CacheNode(String name, Backend backend) {
        this.name = name;
        this.backend = backend;
    }

    public String get(String key) {
        if (cache.containsKey(key)) {
            return cache.get(key);
        }
        String value = backend.read(key);
        cache.put(key, value);
        return value;
    }

    /** Writes go straight to the backend and to this node's own local cache. */
    public void write(String key, String value) {
        backend.write(key, value);
        cache.put(key, value);
    }

    public String getName() {
        return name;
    }
}
