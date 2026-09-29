import java.util.HashMap;
import java.util.Map;

/**
 * A cache node whose entries expire after a fixed TTL, forcing a refetch
 * from the backend once the entry's age exceeds TTL_TICKS. This bounds
 * staleness -- an entry can never be more than TTL_TICKS ticks out of date --
 * but it does nothing to shrink the staleness *inside* that window, because
 * expiry is driven purely by an entry's own age, not by whether the backend
 * actually changed.
 */
public class CacheNode {
    public static final int TTL_TICKS = 5;

    private static class Entry {
        final String value;
        final int cachedAtTick;

        Entry(String value, int cachedAtTick) {
            this.value = value;
            this.cachedAtTick = cachedAtTick;
        }
    }

    private final String name;
    private final Backend backend;
    private final Map<String, Entry> cache = new HashMap<>();

    public CacheNode(String name, Backend backend) {
        this.name = name;
        this.backend = backend;
    }

    public String get(String key, int currentTick) {
        Entry entry = cache.get(key);
        if (entry != null && (currentTick - entry.cachedAtTick) < TTL_TICKS) {
            return entry.value;
        }
        String value = backend.read(key);
        cache.put(key, new Entry(value, currentTick));
        return value;
    }

    public void write(String key, String value, int currentTick) {
        backend.write(key, value);
        cache.put(key, new Entry(value, currentTick));
    }

    public String getName() {
        return name;
    }
}
