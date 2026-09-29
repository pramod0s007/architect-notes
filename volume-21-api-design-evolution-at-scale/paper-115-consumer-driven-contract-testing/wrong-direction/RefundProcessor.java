import java.util.Map;

/**
 * A second, real downstream consumer the provider team does not track:
 * it issues a refund whenever an order's status is exactly "CANCELLED."
 * Its expectations were never encoded anywhere the provider's own tests
 * could see.
 */
public class RefundProcessor {
    public String decide(Map<String, Object> order) {
        Object status = order.get("status");
        if ("CANCELLED".equals(status)) {
            return "REFUND ISSUED for order " + order.get("id");
        }
        return "no refund: status=" + status;
    }
}
