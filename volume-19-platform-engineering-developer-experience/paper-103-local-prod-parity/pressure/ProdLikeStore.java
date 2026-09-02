import java.util.HashMap;
import java.util.Map;

// Represents how the real production database actually behaves: its
// configured collation treats usernames case-INSENSITIVELY, so "Alice" and
// "alice" are the same key as far as a uniqueness check is concerned.
class ProdLikeStore {
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
