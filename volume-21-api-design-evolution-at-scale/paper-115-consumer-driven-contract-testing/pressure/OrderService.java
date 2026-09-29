import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The order-status endpoint, represented as a plain key/value response --
 * exactly how a real JSON API response looks to a consumer: there is no
 * compiler to catch a renamed key, only whatever the consumer's code
 * happens to look up at runtime.
 */
public class OrderService {
    /** The original, currently-deployed contract. */
    public Map<String, Object> getOrderV1(String orderId) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", orderId);
        response.put("total", 108.00);
        response.put("status", "SHIPPED");
        return response;
    }

    /**
     * A code reviewer suggests renaming "status" to "orderStatus" for clarity
     * across the API surface -- a reasonable-sounding cleanup, reviewed and
     * approved by the provider team.
     */
    public Map<String, Object> getOrderV2(String orderId) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", orderId);
        response.put("total", 108.00);
        response.put("orderStatus", "SHIPPED"); // renamed from "status"
        return response;
    }
}
