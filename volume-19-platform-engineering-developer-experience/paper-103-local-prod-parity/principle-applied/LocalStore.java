import java.util.HashMap;
import java.util.Map;

// Local dev now matches production's real, specific behavior that actually
// matters here -- case-insensitive username comparison -- not a full
// replica of production's scale or infrastructure, just the one property
// that determines whether this code behaves correctly.
class LocalStore {
    private final Map<String, String> usersByUsername = new HashMap<>();

    boolean create(String username, String data) {
        String normalized = username.toLowerCase();
        if (usersByUsername.containsKey(normalized)) {
            return false; // rejected, duplicate
        }
        usersByUsername.put(normalized, data);
        return true;
    }

    int size() {
        return usersByUsername.size();
    }
}
