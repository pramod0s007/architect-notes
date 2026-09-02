import java.util.HashMap;
import java.util.Map;

// Because FullParityStore is too expensive to run on every local save, the
// team's actual day-to-day iteration falls back to this lightweight store
// instead -- the same case-sensitive comparison the original bug came from,
// still in daily use because the correct store is reserved for pre-merge.
class FallbackLocalStore {
    private final Map<String, String> usersByUsername = new HashMap<>();

    boolean create(String username, String data) {
        if (usersByUsername.containsKey(username)) {
            return false;
        }
        usersByUsername.put(username, data);
        return true;
    }

    int size() {
        return usersByUsername.size();
    }
}
