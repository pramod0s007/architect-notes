import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A textbook LRU cache: eviction is based purely on recency of access,
 * with no regard for how often a key has historically been used. Backed
 * by LinkedHashMap's built-in access-order mode, which is exactly what a
 * production LRU cache implementation typically relies on.
 */
public class LruCache {
    private final int capacity;
    private final LinkedHashMap<String, Boolean> store;

    public LruCache(int capacity) {
        this.capacity = capacity;
        this.store = new LinkedHashMap<String, Boolean>(capacity, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Boolean> eldest) {
                return size() > LruCache.this.capacity;
            }
        };
    }

    /** Returns true if the key was already cached (a hit), false if it was a miss. */
    public boolean access(String key) {
        boolean hit = store.containsKey(key);
        store.put(key, true); // marks as most-recently-used regardless of hit or miss
        return hit;
    }

    public boolean contains(String key) {
        return store.containsKey(key);
    }
}
