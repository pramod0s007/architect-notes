import java.util.ArrayList;
import java.util.List;

/**
 * WRONG DIRECTION: "fix" the ordering problem by registering the fraud check before
 * shipping, and having shipping check a shared fraud-flagged variable before scheduling.
 * This produces the right outcome for the FIRST order -- but only because of registration
 * order, which was never an enforced dependency, just a coincidence of which two listeners
 * happened to exist at the time. Months later, an unrelated team adds a new listener for
 * the same OrderPlaced event, registered ahead of the fraud check, because nothing in the
 * system says it can't be. The same unprotected race reappears, silently.
 */
public class Main {
    public static void main(String[] args) {
        EventBus bus = new EventBus();
        List<String> executionLog = new ArrayList<>();
        boolean[] fraudFlagged = {false};

        bus.register("OrderPlaced", event -> {
            fraudFlagged[0] = true;
            executionLog.add("FraudCheckService: flagged order " + event.getOrderId() + " as fraudulent");
        });
        bus.register("OrderPlaced", event -> {
            if (fraudFlagged[0]) {
                executionLog.add("ShippingService: fraud flagged -- shipment NOT scheduled");
            } else {
                executionLog.add("ShippingService: scheduled shipment for " + event.getOrderId());
            }
        });

        System.out.println("--- First order: registration order happens to protect it ---");
        bus.publish(new Event("OrderPlaced", "order-77"));
        for (String line : executionLog) {
            System.out.println(line);
        }

        // Months later: a new, unrelated listener is added for the same event, registered
        // BEFORE the fraud check -- nothing in the system prevents this, because there was
        // never an actual enforced dependency, only a registration-order coincidence.
        List<String> laterLog = new ArrayList<>();
        boolean[] laterFraudFlagged = {false};
        EventBus laterBus = new EventBus();
        laterBus.register("OrderPlaced", event ->
                laterLog.add("PriorityShippingService: expedited shipment scheduled for " + event.getOrderId()));
        laterBus.register("OrderPlaced", event -> {
            laterFraudFlagged[0] = true;
            laterLog.add("FraudCheckService: flagged order " + event.getOrderId() + " as fraudulent");
        });
        laterBus.register("OrderPlaced", event -> {
            if (laterFraudFlagged[0]) {
                laterLog.add("ShippingService: fraud flagged -- shipment NOT scheduled");
            } else {
                laterLog.add("ShippingService: scheduled shipment for " + event.getOrderId());
            }
        });

        System.out.println();
        System.out.println("--- Later: a new, unrelated listener is added ahead of the fraud check ---");
        laterBus.publish(new Event("OrderPlaced", "order-88"));
        for (String line : laterLog) {
            System.out.println(line);
        }
    }
}
