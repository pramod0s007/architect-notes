/**
 * V2 handler: updates inventory when an order is placed.
 * Registered with EventBus — did not exist in v1.
 */
public class InventoryHandler {

    public void handle(OrderEvent event) {
        System.out.println("[Inventory] Reserving stock for order "
                + event.getOrderId() + " (amount=$" + event.getAmount() + ")");
    }
}
