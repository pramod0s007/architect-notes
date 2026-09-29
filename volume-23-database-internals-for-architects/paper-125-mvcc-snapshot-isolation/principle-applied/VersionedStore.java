import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * An account store using multi-version concurrency control: every
 * write appends a new, timestamped version of a key rather than
 * overwriting anything, and a read transaction is pinned to a single
 * snapshot timestamp captured when it began. A read at that snapshot
 * always returns the newest version committed at or before it -- so
 * writes that commit after the snapshot began are simply invisible to
 * it, no matter when the read is issued.
 */
public class VersionedStore {
    private static class Version {
        final long commitTs;
        final int value;

        Version(long commitTs, int value) {
            this.commitTs = commitTs;
            this.value = value;
        }
    }

    private final Map<String, List<Version>> versions = new HashMap<>();
    private final AtomicLong clock = new AtomicLong(0);

    /** Commits a new version of a key and returns the commit timestamp it was written at. */
    public synchronized long write(String key, int value) {
        long ts = clock.incrementAndGet();
        versions.computeIfAbsent(key, k -> new ArrayList<>()).add(new Version(ts, value));
        return ts;
    }

    /** The current clock value -- used as a transaction's snapshot timestamp when it begins. */
    public long currentTimestamp() {
        return clock.get();
    }

    /** Returns the newest version of key committed at or before snapshotTs. */
    public synchronized int readAsOf(String key, long snapshotTs) {
        List<Version> history = versions.get(key);
        int result = Integer.MIN_VALUE;
        for (Version v : history) {
            if (v.commitTs <= snapshotTs) {
                result = v.value; // history is append-only in commit order, so this ends on the newest visible version
            }
        }
        return result;
    }
}
