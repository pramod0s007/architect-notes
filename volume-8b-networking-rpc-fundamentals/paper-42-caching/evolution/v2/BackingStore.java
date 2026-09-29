import java.util.concurrent.atomic.AtomicInteger;

/**
 * Stands in for a real backing store, same as evolution/v1's BackingStore,
 * with a deliberately slow load() (simulated via a short sleep) so that
 * concurrent cache misses have a real window to overlap in.
 */
public class BackingStore {
    private volatile String value;
    private final AtomicInteger loadCount = new AtomicInteger(0);

    public BackingStore(String initialValue) {
        this.value = initialValue;
    }

    public String load(String key) {
        loadCount.incrementAndGet();
        try {
            Thread.sleep(50); // simulates a real, non-instant database query
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return value;
    }

    public int getLoadCount() {
        return loadCount.get();
    }
}
