// VIOLATION: One class owns four distinct concerns.
// Inventory team, logistics team, customer comms team, and analytics team
// all demand changes to this single class.
public class OrderFulfillmentProcessor {

    public String fulfill(ShipmentOrder order) {
        // ── Concern 1: Inventory (Inventory Team owns this logic) ──────────
        if (order.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        int available = 100; // simulated warehouse stock
        if (available < order.getQuantity()) {
            throw new IllegalStateException("Insufficient stock for SKU: " + order.getProductSku());
        }
        System.out.println("[Inventory] Reserved " + order.getQuantity()
                + " units of " + order.getProductSku());

        // ── Concern 2: Shipping (Logistics Team owns this logic) ───────────
        if (order.getDestinationAddress() == null || order.getDestinationAddress().isBlank()) {
            throw new IllegalArgumentException("Destination address required");
        }
        String trackingId = "TRK-" + order.getId().toUpperCase();
        System.out.println("[Shipping] Dispatched to " + order.getDestinationAddress()
                + " | tracking=" + trackingId);

        // ── Concern 3: Customer notification (CX Team owns this logic) ─────
        String subject = "Your order " + order.getId() + " is on its way!";
        String body    = "Hi, your " + order.getQuantity() + "x " + order.getProductSku()
                       + " has been shipped. Track: " + trackingId;
        System.out.println("[Email] → " + order.getCustomerEmail()
                + " | " + subject);

        // ── Concern 4: Analytics (Data Team owns this logic) ───────────────
        System.out.println("[Analytics] fulfillment_complete customer="
                + order.getCustomerId() + " sku=" + order.getProductSku()
                + " qty=" + order.getQuantity());

        return trackingId;
    }
}
