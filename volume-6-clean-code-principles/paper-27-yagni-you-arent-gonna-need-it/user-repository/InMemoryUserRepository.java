import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * YAGNI: the right starting point.
 * HashMap lookup is O(1) and sub-millisecond for 50 users.
 * No caching layer, no TTL, no metrics, no eviction thread.
 * Add those when a profiler shows you need them — not before.
 */
public class InMemoryUserRepository implements UserRepository {

    private final Map<String, User> store = new HashMap<>();

    @Override
    public User findById(String id) {
        return store.get(id);
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public void save(User user) {
        store.put(user.getId(), user);
    }
}
