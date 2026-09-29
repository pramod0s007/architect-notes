import java.util.Map;

/** Same RefundProcessor consumer as the wrong-direction scenario. */
public class RefundProcessor {
    public String decide(Map<String, Object> order) {
        Object status = order.get("status");
        if ("CANCELLED".equals(status)) {
            return "REFUND ISSUED for order " + order.get("id");
        }
        return "no refund: status=" + status;
    }
}
