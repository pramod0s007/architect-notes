import java.util.HashMap;
import java.util.Map;

/**
 * A database that only acknowledges a write after its log entry is
 * confirmed durable -- the log append happens first and is synchronous,
 * and only once it returns does the write get applied to the in-memory
 * table and reported as acknowledged. There is no window where an
 * acknowledged write's log entry could still be lost.
 */
public class Database {
    private final WriteAheadLog wal = new WriteAheadLog();
    private final Map<String, String> table = new HashMap<>();

    public boolean put(String key, String value) {
        wal.appendAndFsync(key + "=" + value); // durable before we proceed
        table.put(key, value);
        return true; // acknowledged only after durability is confirmed
    }

    public WriteAheadLog wal() {
        return wal;
    }
}
