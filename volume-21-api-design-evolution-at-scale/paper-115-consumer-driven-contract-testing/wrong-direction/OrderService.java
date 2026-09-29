import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The order-status endpoint, now covering both a shipped order and a
 * cancelled order. The provider team, chastened by the previous incident,
 * fixed the "status" regression -- but has no visibility into every
 * consumer's exact expectations for every status value, only the ones
 * they happen to think to test.
 */
public class OrderService {
    public Map<String, Object> getShippedOrderV1(String orderId) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", orderId);
        response.put("total", 108.00);
        response.put("status", "SHIPPED");
        return response;
    }

    public Map<String, Object> getCancelledOrderV1(String orderId) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", orderId);
        response.put("total", 108.00);
        response.put("status", "CANCELLED");
        return response;
    }

    /** An unrelated later change: a spelling cleanup standardizes on US spelling across the API. */
    public Map<String, Object> getCancelledOrderV2(String orderId) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", orderId);
        response.put("total", 108.00);
        response.put("status", "CANCELED"); // "CANCELLED" -> "CANCELED", single L
        return response;
    }
}
