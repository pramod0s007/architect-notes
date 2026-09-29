import java.util.HashMap;
import java.util.Map;

/**
 * A database with no write-ahead log or any other durability mechanism
 * at all: writes go straight into an in-memory map and are acknowledged
 * immediately. Nothing about a write ever reaches disk, so the map's
 * entire contents exist only as long as the process does.
 */
public class Database {
    private final Map<String, String> table = new HashMap<>();

    /** Applies the write and reports it acknowledged -- nothing here is durable. */
    public boolean put(String key, String value) {
        table.put(key, value);
        return true; // acknowledged
    }

    public int size() {
        return table.size();
    }
}
