/**
 * V1: single handler — no event bus needed yet.
 * Called directly from OrderService. YAGNI respected.
 */
public class OrderPlacedHandler {

    public void handleOrderPlaced(OrderEvent event) {
        System.out.println("[Confirmation] Order " + event.getOrderId()
                + " confirmed, $" + event.getAmount());
        System.out.println("[Inventory]    Reserving stock for order " + event.getOrderId());
    }
}
