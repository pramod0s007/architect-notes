import java.util.concurrent.ConcurrentHashMap;

/**
 * A read-through cache where every entry carries an expiration timestamp.
 * A request for an expired entry is treated exactly like a cache miss: it
 * calls the backing store again and re-caches the fresh result.
 *
 * This fixes permanent staleness -- but a miss is still a miss, and if many
 * threads ask for the same key at the same instant right after it expires,
 * every one of them independently sees "not cached" and independently calls
 * the (slow) backing store.
 */
public class TtlCache {
    private static class Entry {
        final String value;
        final long expiresAtMs;

        Entry(String value, long expiresAtMs) {
            this.value = value;
            this.expiresAtMs = expiresAtMs;
        }
    }

    private final ConcurrentHashMap<String, Entry> store = new ConcurrentHashMap<>();
    private final BackingStore backingStore;
    private final long ttlMs;

    public TtlCache(BackingStore backingStore, long ttlMs) {
        this.backingStore = backingStore;
        this.ttlMs = ttlMs;
    }

    public String get(String key) {
        Entry entry = store.get(key);
        long now = System.currentTimeMillis();
        if (entry != null && entry.expiresAtMs > now) {
            return entry.value;
        }
        String value = backingStore.load(key);
        store.put(key, new Entry(value, now + ttlMs));
        return value;
    }
}
