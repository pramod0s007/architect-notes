import java.util.UUID;

/**
 * V2 — EventBus introduced when the second handler arrived, not before.
 *
 * OrderService v1 served the system until AuditHandler was required.
 * At that point, adding it to a direct call would mean OrderService knew
 * about both handlers — EventBus cleanly decouples them.
 * Comment: EventBus introduced when second handler arrived — not before.
 */
public class OrderServiceV2 {

    private final EventBus eventBus;

    public OrderServiceV2(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public String placeOrder(String customerId, double amount) {
        String orderId = "ORD-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        System.out.println("[OrderService v2] Placing order " + orderId + " for customer " + customerId);
        OrderEvent event = new OrderEvent(orderId, "ORDER_PLACED", amount);
        eventBus.publish("ORDER_PLACED", event);
        return orderId;
    }
}
