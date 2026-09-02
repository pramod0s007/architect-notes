import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    static Map<String, String> cache = new HashMap<>();
    static int databaseFetchCount = 0;

    // Overcorrection: reads ARE correctly tenant-scoped -- this isn't the
    // pressure case's mistake, there is no collision on read. But the
    // invalidation path, written to "clear this product from the cache
    // when it changes," was implemented generically across every tenant,
    // because product IDs look like a single, shared kind of thing to
    // invalidate -- they aren't.
    static String cacheKey(String tenantId, String productId) {
        return tenantId + ":product:" + productId;
    }

    static String getProduct(String tenantId, String productId, String realData) {
        String key = cacheKey(tenantId, productId);
        if (cache.containsKey(key)) {
            return cache.get(key);
        }
        databaseFetchCount++;
        cache.put(key, realData);
        return realData;
    }

    static void invalidateProductEverywhere(String productId, List<String> allTenantIds) {
        for (String tenantId : allTenantIds) {
            cache.remove(cacheKey(tenantId, productId));
        }
    }

    public static void main(String[] args) {
        List<String> allTenants = List.of("TenantA", "TenantB", "TenantC");

        System.out.println("All three tenants load their own, unrelated product 100 (each caches independently):");
        getProduct("TenantA", "100", "Widget A - $50");
        getProduct("TenantB", "100", "Gadget B - $75");
        getProduct("TenantC", "100", "Doohickey C - $30");
        System.out.println("  Database fetches so far: " + databaseFetchCount + " (one per tenant, all now cached)");

        System.out.println();
        System.out.println("TenantA updates its OWN product 100. The invalidation logic clears product 100");
        System.out.println("from the cache for every tenant, not just TenantA:");
        invalidateProductEverywhere("100", allTenants);

        System.out.println();
        System.out.println("TenantB and TenantC re-request their own product 100 -- their data never changed:");
        getProduct("TenantB", "100", "Gadget B - $75");
        getProduct("TenantC", "100", "Doohickey C - $30");
        System.out.println("  Database fetches now: " + databaseFetchCount);

        System.out.println();
        System.out.println(databaseFetchCount == 5
            ? "CONFIRMED FAILURE: TenantB and TenantC were forced to refetch their completely unchanged "
              + "product data from the database, purely because TenantA updated its own, unrelated "
              + "product. No data leaked -- reads are correctly tenant-scoped -- but the invalidation "
              + "path scoped itself by product ID alone, so one tenant's write destroyed two other "
              + "tenants' valid cache entries and forced two unnecessary database round-trips."
            : "Unexpected -- investigate.");
    }
}
