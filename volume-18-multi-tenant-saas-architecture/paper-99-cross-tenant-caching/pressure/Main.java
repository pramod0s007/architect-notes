import java.util.HashMap;
import java.util.Map;

public class Main {
    // Cache key built from the resource ID alone. Product IDs are assigned
    // independently per tenant, so "product 100" for TenantA and "product
    // 100" for TenantB are two completely different, unrelated records that
    // just happen to share a number.
    static Map<String, String> cache = new HashMap<>();

    static String cacheKey(String productId) {
        return "product:" + productId;
    }

    static String getProduct(String tenantId, String productId, String realData) {
        String key = cacheKey(productId);
        if (cache.containsKey(key)) {
            return cache.get(key); // whoever cached this key first, everyone gets
        }
        cache.put(key, realData);
        return realData;
    }

    public static void main(String[] args) {
        System.out.println("TenantA requests its own product 100:");
        String aResult = getProduct("TenantA", "100", "Widget A - $50 (TenantA's real product)");
        System.out.println("  Returned: " + aResult);

        System.out.println();
        System.out.println("TenantB requests ITS OWN, completely different product, which also happens to be ID 100:");
        String bResult = getProduct("TenantB", "100", "Gadget B - $75 (TenantB's real product)");
        System.out.println("  Returned: " + bResult);

        System.out.println();
        System.out.println(bResult.contains("TenantA")
            ? "CONFIRMED FAILURE: TenantB asked for its own product 100 and received TenantA's product "
              + "data instead. The cache key was built from the product ID alone; TenantA's and TenantB's "
              + "unrelated records collided on the exact same key, and the cache served whichever tenant "
              + "asked first to whichever tenant asked second."
            : "Unexpected -- investigate.");
    }
}
