import java.util.HashMap;
import java.util.Map;

/**
 * A database backed by a write-ahead log, but one that acknowledges a
 * write as soon as the log entry is buffered -- before that entry is
 * necessarily flushed (fsynced) to durable storage. This looks like a
 * WAL-backed, durable design, but the acknowledgment point is wrong:
 * it happens before durability is actually guaranteed, not after.
 */
public class Database {
    private final WriteAheadLog wal = new WriteAheadLog();
    private final Map<String, String> table = new HashMap<>();

    public boolean put(String key, String value) {
        wal.append(key + "=" + value); // buffered, not necessarily durable yet
        table.put(key, value);
        return true; // acknowledged immediately, regardless of flush state
    }

    public WriteAheadLog wal() {
        return wal;
    }
}
