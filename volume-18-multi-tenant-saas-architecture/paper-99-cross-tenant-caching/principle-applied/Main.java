import java.util.HashMap;
import java.util.Map;

public class Main {
    static Map<String, String> cache = new HashMap<>();

    // The cache key includes the tenant ID -- TenantA's product 100 and
    // TenantB's product 100 can never collide, because they are, as far as
    // the cache is concerned, entirely different keys.
    static String cacheKey(String tenantId, String productId) {
        return tenantId + ":product:" + productId;
    }

    static String getProduct(String tenantId, String productId, String realData) {
        String key = cacheKey(tenantId, productId);
        if (cache.containsKey(key)) {
            return cache.get(key);
        }
        cache.put(key, realData);
        return realData;
    }

    public static void main(String[] args) {
        System.out.println("TenantA requests its own product 100:");
        String aResult = getProduct("TenantA", "100", "Widget A - $50 (TenantA's real product)");
        System.out.println("  Returned: " + aResult);

        System.out.println();
        System.out.println("TenantB requests ITS OWN, completely different product, also ID 100:");
        String bResult = getProduct("TenantB", "100", "Gadget B - $75 (TenantB's real product)");
        System.out.println("  Returned: " + bResult);

        System.out.println();
        System.out.println(bResult.contains("TenantB")
            ? "FIXED: TenantB received its own product data, not TenantA's. Including the tenant ID in "
              + "the cache key means the two records were never competing for the same slot -- there was "
              + "never a collision to have in the first place."
            : "Unexpected -- investigate.");
    }
}
