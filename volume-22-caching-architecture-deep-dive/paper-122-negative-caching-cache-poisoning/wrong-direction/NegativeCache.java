import java.util.HashMap;
import java.util.Map;

/**
 * A cache that memoizes negative lookups (a key that doesn't exist)
 * forever, with no expiry and no invalidation hook. This is efficient --
 * repeated lookups for the same missing key hit the backend only once --
 * but it has no way to learn that a key which didn't exist before might
 * exist now, because nothing about creating a record ever touches this
 * cache.
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
}
