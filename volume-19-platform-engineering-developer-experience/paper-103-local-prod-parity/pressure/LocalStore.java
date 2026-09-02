import java.util.HashMap;
import java.util.Map;

// A lightweight stand-in for the real database, used for local development.
// It compares usernames with plain, case-sensitive string equality --
// nobody configured it to match production's actual collation, because it
// was built to be fast and simple, not to be faithful.
class LocalStore {
    private final Map<String, String> usersByUsername = new HashMap<>();

    boolean create(String username, String data) {
        if (usersByUsername.containsKey(username)) {
            return false; // rejected, duplicate
        }
        usersByUsername.put(username, data);
        return true;
    }

    int size() {
        return usersByUsername.size();
    }
}
