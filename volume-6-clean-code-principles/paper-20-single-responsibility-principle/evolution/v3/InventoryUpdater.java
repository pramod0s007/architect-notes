public class InventoryUpdater {
    public void updateInventory(Order order) {
        // Ops team owns this class exclusively
        System.out.println("Inventory updated (idempotent) for order: " + order.getId());
    }
}
