import java.util.HashMap;
import java.util.Map;

/**
 * A read-through cache with no expiration and no invalidation: once a key is
 * loaded, it stays in the cache forever, no matter what happens to the
 * underlying data afterward.
 */
public class NaiveCache {
    private final Map<String, String> store = new HashMap<>();
    private final BackingStore backingStore;

    public NaiveCache(BackingStore backingStore) {
        this.backingStore = backingStore;
    }

    public String get(String key) {
        if (store.containsKey(key)) {
            return store.get(key);
        }
        String value = backingStore.load(key);
        store.put(key, value);
        return value;
    }
}
