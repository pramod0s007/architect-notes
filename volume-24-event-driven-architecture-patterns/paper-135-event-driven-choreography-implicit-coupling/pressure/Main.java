import java.util.ArrayList;
import java.util.List;

/**
 * PRESSURE: two independent services both react to the same OrderPlaced event. Shipping
 * happens to have been registered before the fraud check -- an accident of which module
 * was wired up first, not a deliberate decision. Because choreography has no central
 * coordinator, execution order for reactions to the same event is entirely a function of
 * registration order, and nothing here encodes that shipping should actually depend on
 * the fraud check completing first.
 */
public class Main {
    public static void main(String[] args) {
        EventBus bus = new EventBus();
        List<String> executionLog = new ArrayList<>();

        bus.register("OrderPlaced", event ->
                executionLog.add("ShippingService: scheduled shipment for " + event.getOrderId()));
        bus.register("OrderPlaced", event ->
                executionLog.add("FraudCheckService: flagged order " + event.getOrderId() + " as fraudulent"));

        bus.publish(new Event("OrderPlaced", "order-77"));

        for (String line : executionLog) {
            System.out.println(line);
        }
        System.out.println();
        System.out.println("Shipment was scheduled BEFORE the fraud check even ran -- nothing prevented it.");
    }
}
