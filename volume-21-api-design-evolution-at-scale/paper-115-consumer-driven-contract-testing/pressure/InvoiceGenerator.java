import java.util.Map;

/**
 * A real downstream consumer of the order-status endpoint: it generates an
 * invoice whenever an order's status is "SHIPPED." Nothing about this
 * class is visible to, or tested by, the provider team.
 */
public class InvoiceGenerator {
    public String decide(Map<String, Object> order) {
        Object status = order.get("status");
        if ("SHIPPED".equals(status)) {
            return "INVOICE GENERATED for order " + order.get("id");
        }
        return "no invoice: status=" + status;
    }
}
