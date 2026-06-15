import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

// YAGNI violation: caching infrastructure built before any performance problem
// was measured. The system currently serves 50 internal users — HashMap lookup
// takes ~0.001ms. This adds 80 lines of threading, TTL tracking, and metrics
// that will never justify their maintenance cost at this scale.

public class PrematureCachingRepository implements UserRepository {

    private static final long TTL_MS = 60_000;

    private final Map<String, User>    primaryStore = new HashMap<>();
    private final ConcurrentHashMap<String, User>  l1Cache      = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long>  cacheTimestamps = new ConcurrentHashMap<>();

    // YAGNI: metrics for a cache that isn't needed yet
    private final AtomicLong cacheHits   = new AtomicLong();
    private final AtomicLong cacheMisses = new AtomicLong();

    public PrematureCachingRepository() {
        // YAGNI: eviction thread scheduled at startup for a 50-user system
        Executors.newSingleThreadScheduledExecutor()
                 .scheduleAtFixedRate(this::evictExpired, 1, 1, TimeUnit.MINUTES);
    }

    @Override
    public User findById(String id) {
        Long ts = cacheTimestamps.get(id);
        if (ts != null && System.currentTimeMillis() - ts < TTL_MS) {
            cacheHits.incrementAndGet();
            return l1Cache.get(id);
        }
        cacheMisses.incrementAndGet();
        User user = primaryStore.get(id);
        if (user != null) {
            l1Cache.put(id, user);
            cacheTimestamps.put(id, System.currentTimeMillis());
        }
        return user;
    }

    @Override
    public List<User> findAll() {
        // YAGNI: cache warming on list — also invalidates the cache on every findAll
        primaryStore.forEach((id, u) -> {
            l1Cache.put(id, u);
            cacheTimestamps.put(id, System.currentTimeMillis());
        });
        return new ArrayList<>(primaryStore.values());
    }

    @Override
    public void save(User user) {
        primaryStore.put(user.getId(), user);
        l1Cache.put(user.getId(), user);
        cacheTimestamps.put(user.getId(), System.currentTimeMillis());
    }

    private void evictExpired() {
        long now = System.currentTimeMillis();
        cacheTimestamps.entrySet().removeIf(entry -> {
            if (now - entry.getValue() > TTL_MS) {
                l1Cache.remove(entry.getKey());
                return true;
            }
            return false;
        });
    }

    public String cacheStats() {
        return "hits=" + cacheHits.get() + " misses=" + cacheMisses.get();
    }
}
