import java.util.Map;

/** Same InvoiceGenerator consumer as the pressure scenario. */
public class InvoiceGenerator {
    public String decide(Map<String, Object> order) {
        Object status = order.get("status");
        if ("SHIPPED".equals(status)) {
            return "INVOICE GENERATED for order " + order.get("id");
        }
        return "no invoice: status=" + status;
    }
}
