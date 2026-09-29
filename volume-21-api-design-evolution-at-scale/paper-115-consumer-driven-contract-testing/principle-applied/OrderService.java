import java.util.LinkedHashMap;
import java.util.Map;

/** Same order-status endpoint as the wrong-direction scenario. */
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

    /** The same spelling-cleanup change attempted in the wrong-direction scenario. */
    public Map<String, Object> getCancelledOrderV2(String orderId) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", orderId);
        response.put("total", 108.00);
        response.put("status", "CANCELED"); // "CANCELLED" -> "CANCELED", single L
        return response;
    }
}
