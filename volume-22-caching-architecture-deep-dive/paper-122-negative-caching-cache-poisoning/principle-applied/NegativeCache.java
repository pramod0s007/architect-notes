import java.util.HashMap;
import java.util.Map;

/**
 * A cache that memoizes negative lookups just like the wrong-direction
 * version, but adds one thing: creating a record goes through this same
 * cache, and creation explicitly invalidates any negative entry for that
 * key before writing to the backend. The efficiency win is unchanged --
 * repeated lookups for a genuinely missing key still cost one backend
 * call -- but a key that gets created is never stuck behind a stale
 * "not found" entry, because the write itself is what clears it.
 */
public class NegativeCache {
    private static final Object NOT_FOUND = new Object();

    private final Backend backend;
    private final Map<String, Object> cache = new HashMap<>();

    public NegativeCache(Backend backend) {
        this.backend = backend;
    }

    public String lookup(String key) {
        Object cached = cache.get(key);
        if (cached == NOT_FOUND) {
            return null;
        }
        if (cached != null) {
            return (String) cached;
        }
        String value = backend.lookup(key);
        cache.put(key, value == null ? NOT_FOUND : value);
        return value;
    }

    /**
     * Creates the record in the backend and removes any negative cache
     * entry for this key, so the very next lookup is guaranteed to see
     * the record rather than a stale "not found".
     */
    public void create(String key, String value) {
        backend.create(key, value);
        cache.remove(key);
    }
}
