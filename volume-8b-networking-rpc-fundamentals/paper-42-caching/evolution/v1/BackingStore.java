import java.util.concurrent.atomic.AtomicInteger;

/**
 * Stands in for a real backing store (a database, a downstream service) --
 * a single mutable value that can be updated independently of the cache
 * sitting in front of it, plus a counter of how many times it was actually
 * queried, used to prove how many times the cache missed.
 */
public class BackingStore {
    private volatile String value;
    private final AtomicInteger loadCount = new AtomicInteger(0);

    public BackingStore(String initialValue) {
        this.value = initialValue;
    }

    public String load(String key) {
        loadCount.incrementAndGet();
        return value;
    }

    public void update(String newValue) {
        this.value = newValue;
    }

    public int getLoadCount() {
        return loadCount.get();
    }
}
