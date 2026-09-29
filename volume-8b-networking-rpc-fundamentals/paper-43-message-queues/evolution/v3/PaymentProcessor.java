import java.util.HashMap;
import java.util.Map;

/**
 * Stands in for a downstream payment service. Tracks how many times each
 * order has actually been charged, so the demo can prove whether a
 * redelivered message caused a real duplicate charge or not.
 */
public class PaymentProcessor {
    private final Map<String, Integer> chargeCounts = new HashMap<>();

    public void charge(String orderId, double amount) {
        chargeCounts.merge(orderId, 1, Integer::sum);
        System.out.println("Charged " + orderId + " for $" + amount + " (charge #"
                + chargeCounts.get(orderId) + " for this order)");
    }

    public int getChargeCount(String orderId) {
        return chargeCounts.getOrDefault(orderId, 0);
    }
}
