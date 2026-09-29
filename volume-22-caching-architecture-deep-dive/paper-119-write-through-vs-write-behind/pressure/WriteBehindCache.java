import java.util.ArrayList;
import java.util.List;

/**
 * A write-behind (write-back) cache: every write is accepted and
 * acknowledged immediately, buffered only in memory, and applied to the
 * backend later in a scheduled batch flush. This is fast -- no write
 * pays the cost of a backend round trip -- but the buffered writes exist
 * only in this process's memory until the next scheduled flush actually
 * runs.
 */
public class WriteBehindCache {
    private final Backend backend;
    private final List<String> pendingWrites = new ArrayList<>();

    public WriteBehindCache(Backend backend) {
        this.backend = backend;
    }

    /** Accepts a write immediately; it is NOT yet durable in the backend. */
    public void write(String key, String value) {
        pendingWrites.add(key);
    }

    public int pendingWriteCount() {
        return pendingWrites.size();
    }

    /** The scheduled flush: applies every buffered write to the backend. */
    public void flush() {
        for (String key : pendingWrites) {
            backend.write(key, "value-for-" + key);
        }
        pendingWrites.clear();
    }
}
