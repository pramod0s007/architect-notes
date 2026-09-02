import java.util.HashMap;
import java.util.Map;

// Overcorrection: this store matches production's collation correctly --
// that part isn't the pressure case's mistake. But "parity" here was taken
// to mean replicating production's full data scale too, not just the one
// behavior (collation) that actually determines correctness. Every use
// requires seeding a production-scale dataset first.
class FullParityStore {
    private final Map<String, String> usersByUsername = new HashMap<>();

    void provisionProductionScaleDataset(int rowCount) {
        for (int i = 0; i < rowCount; i++) {
            usersByUsername.put("seed-user-" + i, "seed-data-" + i);
        }
    }

    boolean create(String username, String data) {
        String normalized = username.toLowerCase();
        if (usersByUsername.containsKey(normalized)) {
            return false;
        }
        usersByUsername.put(normalized, data);
        return true;
    }

    int size() {
        return usersByUsername.size();
    }
}
