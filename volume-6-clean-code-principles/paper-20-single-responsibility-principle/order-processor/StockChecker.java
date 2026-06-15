// SRP: Inventory Team owns this class. Only they change it.
public class StockChecker {
    private static final int SIMULATED_STOCK = 100;

    public void reserve(ShipmentOrder order) {
        if (order.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (SIMULATED_STOCK < order.getQuantity()) {
            throw new IllegalStateException("Insufficient stock for SKU: " + order.getProductSku());
        }
        System.out.println("[Inventory] Reserved " + order.getQuantity()
                + " units of " + order.getProductSku());
    }
}
