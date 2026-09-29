import java.util.ArrayList;
import java.util.List;

/**
 * A write-behind cache backed by a durable write-ahead log: every write
 * is appended to a cheap, sequential, durable log entry synchronously --
 * before being acknowledged -- and buffered in memory for a later batch
 * flush to the backend, exactly like plain write-behind. The difference
 * is that a crash before the flush no longer loses anything: replaying
 * the durable log recovers every buffered write, because the log entry
 * was made durable at write time, not deferred along with the backend
 * write itself.
 */
public class WriteBehindWithWalCache {
    private final Backend backend;
    private final int logAppendCostTicks;
    private final List<String> pendingWrites = new ArrayList<>();
    private final List<String> durableLog = new ArrayList<>();
    private int ticksSpent = 0;

    public WriteBehindWithWalCache(Backend backend, int logAppendCostTicks) {
        this.backend = backend;
        this.logAppendCostTicks = logAppendCostTicks;
    }

    /** Appends to the durable log synchronously (cheap), then buffers for later backend flush. */
    public void write(String key, String value) {
        durableLog.add(key);
        ticksSpent += logAppendCostTicks;
        pendingWrites.add(key);
    }

    public int ticksSpent() {
        return ticksSpent;
    }

    public int pendingWriteCount() {
        return pendingWrites.size();
    }

    /** The scheduled flush: applies every buffered write to the backend, then clears the log. */
    public void flush() {
        for (String key : pendingWrites) {
            backend.write(key, "value-for-" + key);
        }
        pendingWrites.clear();
        durableLog.clear();
    }

    /** Simulates crash recovery: replays the durable log to restore every buffered write. */
    public int recoverFromCrash() {
        int recovered = durableLog.size();
        for (String key : durableLog) {
            backend.write(key, "value-for-" + key);
        }
        durableLog.clear();
        pendingWrites.clear();
        return recovered;
    }
}
