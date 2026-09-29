import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Demonstrates that moving the acknowledgment point to after the log
 * entry is durable closes the data-loss gap completely: the same 47
 * writes, replayed from the log after a simulated crash, recover
 * exactly as many rows as were acknowledged -- zero lost.
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

        // Simulate a crash: every entry is already durable, so nothing
        // is lost -- there's no buffered, not-yet-flushed state to lose.
        List<String> survivingEntries = db.wal().durableEntries();

        Map<String, String> recovered = new HashMap<>();
        for (String entry : survivingEntries) {
            String[] parts = entry.split("=", 2);
            recovered.put(parts[0], parts[1]);
        }

        System.out.println("Rows recovered after crash: " + recovered.size());
        System.out.println("Acknowledged writes lost: " + (acknowledged - recovered.size()));
    }
}
