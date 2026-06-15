import java.util.UUID;

/**
 * V1 — YAGNI respected.
 * One handler, called directly. No event bus, no subscriber registry,
 * no generic dispatch mechanism. Works perfectly for the current requirement.
 * Evolve to EventBus only when a second handler actually arrives.
 */
public class OrderService {

    private final OrderPlacedHandler handler;

    public OrderService(OrderPlacedHandler handler) {
        this.handler = handler;
    }

    public String placeOrder(String customerId, double amount) {
        String orderId = "ORD-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        System.out.println("[OrderService v1] Placing order " + orderId + " for customer " + customerId);
        OrderEvent event = new OrderEvent(orderId, "ORDER_PLACED", amount);
        handler.handleOrderPlaced(event);
        return orderId;
    }
}
