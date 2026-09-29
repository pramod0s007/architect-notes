import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Demonstrates that a write-ahead log alone doesn't guarantee
 * durability if writes are acknowledged before their log entry is
 * actually flushed. A crash discards anything still sitting in the
 * log's in-memory buffer -- exactly the writes that were told the
 * caller they succeeded.
 */
public class Main {
    public static void main(String[] args) {
        Database db = new Database();

        int totalWrites = 47;
        int acknowledged = 0;
        for (int i = 0; i < totalWrites; i++) {
            if (db.put("key-" + i, "value-" + i)) {
                acknowledged++;
            }
        }
        System.out.println("Writes acknowledged: " + acknowledged);
        System.out.println("Entries still buffered, never flushed: " + db.wal().bufferedCount());

        // Simulate a crash: only the WAL's flushed (durable) entries
        // survive. Anything still sitting in the buffer -- entries that
        // were acknowledged but never actually reached disk -- is gone.
        List<String> survivingEntries = db.wal().durableEntries();

        // Recovery: replay every durable log entry into a fresh table.
        Map<String, String> recovered = new HashMap<>();
        for (String entry : survivingEntries) {
            String[] parts = entry.split("=", 2);
            recovered.put(parts[0], parts[1]);
        }

        System.out.println("Rows recovered after crash: " + recovered.size());
        System.out.println("Acknowledged writes lost: " + (acknowledged - recovered.size()));
    }
}
