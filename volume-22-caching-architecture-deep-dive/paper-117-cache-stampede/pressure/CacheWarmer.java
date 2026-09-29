import java.util.HashMap;
import java.util.Map;

/**
 * Populates a simulated cache with N keys, all warmed at the same instant
 * (tick 0) with a fixed TTL -- exactly what happens after a deploy, a
 * cache flush, or a bulk cache-warming job. Every key's expiry is
 * scheduled with the caller-supplied strategy, so different scenarios can
 * plug in a different expiry-assignment policy without changing anything
 * else about the simulation.
 */
public class CacheWarmer {
    /** Assigns an expiry tick to the i-th key, given the base TTL. */
    public interface ExpiryStrategy {
        int expiryTickFor(int keyIndex, int baseTtl);
    }

    public Map<Integer, Integer> warm(int keyCount, int baseTtl, ExpiryStrategy strategy) {
        Map<Integer, Integer> expiryByKey = new HashMap<>();
        for (int i = 0; i < keyCount; i++) {
            expiryByKey.put(i, strategy.expiryTickFor(i, baseTtl));
        }
        return expiryByKey;
    }
}
