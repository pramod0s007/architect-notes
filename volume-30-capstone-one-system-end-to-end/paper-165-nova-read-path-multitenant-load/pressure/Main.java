import java.util.LinkedHashMap;
import java.util.Map;

/**
 * PRESSURE: Nova's catalog-read path (the multi-tenant order/checkout platform this volume
 * follows) shares one product-lookup cache across all tenants, with no per-tenant isolation.
 * Tenant A is a small, steady tenant whose 20-item hot working set stays in cache under
 * normal load. Tenant B is a large tenant that runs a one-time cold catalog scan touching
 * 100 distinct items it will not look at again soon. Under one shared LRU eviction policy
 * with no tenant boundary, Tenant B's cold scan evicts Tenant A's hot working set entirely,
 * even though Tenant A's own key set and access sequence do not change between its warm
 * check and its recheck -- the only cache activity modeled in between is Tenant B's scan.
 */
public class Main {

    static final int CACHE_CAPACITY = 100;

    public static void main(String[] args) {
        SharedCache cache = new SharedCache(CACHE_CAPACITY);

        System.out.println("-- Nova catalog cache, single shared cache, no tenant isolation --");
        System.out.println("Cache capacity: " + CACHE_CAPACITY + " slots (shared across all tenants)");
        System.out.println();

        // Tenant A establishes its 20-item hot working set (normal, steady traffic).
        for (int i = 0; i < 20; i++) {
            cache.access("tenantA-item-" + i);
        }
        int tenantAWarmHits = 0;
        for (int i = 0; i < 20; i++) {
            if (cache.access("tenantA-item-" + i)) {
                tenantAWarmHits++;
            }
        }
        System.out.println("Tenant A hot working set established: " + tenantAWarmHits
                + "/20 hits on re-access (confirms A's 20 items are resident and hot).");

        // Tenant B runs a one-time cold catalog scan: 100 distinct items, each touched once.
        for (int i = 0; i < 100; i++) {
            cache.access("tenantB-item-" + i);
        }
        System.out.println("Tenant B cold catalog scan complete: 100 distinct items touched once each.");
        System.out.println();

        // Re-check Tenant A's hot working set after Tenant B's cold scan.
        int tenantAHitsAfterBurst = 0;
        for (int i = 0; i < 20; i++) {
            if (cache.access("tenantA-item-" + i)) {
                tenantAHitsAfterBurst++;
            }
        }
        int tenantAHitRatePct = (tenantAHitsAfterBurst * 100) / 20;

        System.out.println("Tenant A hot working set re-checked after Tenant B's cold scan:");
        System.out.println("  Hits: " + tenantAHitsAfterBurst + "/20 (" + tenantAHitRatePct + "%)");
        System.out.println("  Tenant A's own access pattern never changed. Tenant B's one-time cold");
        System.out.println("  scan, sharing the same cache with no tenant boundary, evicted Tenant A's");
        System.out.println("  entire hot working set under the shared LRU policy.");
    }

    /** A single shared LRU cache with no concept of which tenant an item belongs to. */
    static class SharedCache {
        private final LinkedHashMap<String, Boolean> store;

        SharedCache(int capacity) {
            this.store = new LinkedHashMap<>(16, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, Boolean> eldest) {
                    return size() > capacity;
                }
            };
        }

        /** Returns true on a cache hit, false on a miss (and inserts on miss). */
        boolean access(String key) {
            if (store.containsKey(key)) {
                store.get(key); // accessOrder=true moves this entry to most-recently-used
                return true;
            }
            store.put(key, Boolean.TRUE);
            return false;
        }
    }
}
