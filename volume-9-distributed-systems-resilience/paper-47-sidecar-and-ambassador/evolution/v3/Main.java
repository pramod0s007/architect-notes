public class Main {
    public static void main(String[] args) {
        InventoryService inventory = new InventoryService();
        ShippingService shipping = new ShippingService();
        Order order = new Order("ORD-3");

        System.out.println("Inventory (via sidecar): reserved=" + inventory.reserve(order));
        System.out.println("Shipping  (via sidecar): result=" + shipping.schedule(order));
        System.out.println("v3: identical downstream flakiness, identical retry policy --");
        System.out.println("    both succeed, because both use the SAME ResilienceSidecar.");
    }
}
