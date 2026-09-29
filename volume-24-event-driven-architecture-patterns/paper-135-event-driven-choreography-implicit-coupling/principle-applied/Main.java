import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * PRINCIPLE APPLIED: make the real dependency structural instead of implicit. Shipping
 * never listens to OrderPlaced at all -- it listens only to FraudCheckPassed, a distinct
 * event that FraudCheckService explicitly publishes once (and only if) an order clears.
 * The dependency is now encoded as "which event triggers me," not "which order did my
 * listener happen to get registered in." This protects ShippingService itself: no matter
 * how many other listeners are added for OrderPlaced, or in what order, none of them can
 * make ShippingService run early, because it was never listening to that event at all.
 * (It does not, by itself, stop a *different* service from independently choosing to
 * subscribe to OrderPlaced and act on shipment early -- that would be a new instance of
 * the same mistake, not something this pattern can prevent from outside that service.)
 */
public class Main {
    private static final Set<String> FRAUDULENT_ORDERS = new HashSet<>();

    static {
        FRAUDULENT_ORDERS.add("order-77");
    }

    public static void main(String[] args) {
        EventBus bus = new EventBus();
        List<String> executionLog = new ArrayList<>();

        bus.register("OrderPlaced", event -> {
            boolean fraudulent = FRAUDULENT_ORDERS.contains(event.getOrderId());
            executionLog.add("FraudCheckService: evaluated order " + event.getOrderId()
                    + " -- fraudulent: " + fraudulent);
            if (!fraudulent) {
                bus.publish(new Event("FraudCheckPassed", event.getOrderId()));
            }
        });

        bus.register("FraudCheckPassed", event ->
                executionLog.add("ShippingService: scheduled shipment for " + event.getOrderId()));

        // A new, unrelated listener can be added for OrderPlaced freely -- it has no way
        // to affect ShippingService's correctness, because ShippingService was never
        // listening to OrderPlaced in the first place.
        bus.register("OrderPlaced", event ->
                executionLog.add("AuditLogService: recorded order placement for " + event.getOrderId()));

        System.out.println("--- Fraudulent order: shipment correctly never triggered ---");
        bus.publish(new Event("OrderPlaced", "order-77"));
        for (String line : executionLog) {
            System.out.println(line);
        }
        boolean shippedFraudulent = executionLog.stream().anyMatch(l -> l.contains("ShippingService: scheduled"));
        System.out.println("Shipment scheduled for fraudulent order: " + shippedFraudulent);

        executionLog.clear();
        System.out.println();
        System.out.println("--- Clean order: shipment correctly triggered, only after fraud check passes ---");
        bus.publish(new Event("OrderPlaced", "order-99"));
        for (String line : executionLog) {
            System.out.println(line);
        }
        boolean shippedClean = executionLog.stream().anyMatch(l -> l.contains("ShippingService: scheduled"));
        System.out.println("Shipment scheduled for clean order: " + shippedClean);
    }
}
