public class Main {
    public static void main(String[] args) {
        InventoryService inventory = new InventoryService();
        ShippingService shipping = new ShippingService();
        Order order = new Order("ORD-2");

        // Both backends fail exactly twice before succeeding -- identical flakiness.
        System.out.println("Inventory (3 attempts allowed): reserved=" + inventory.reserve(order));
        System.out.println("Shipping  (2 attempts allowed): result=" + shipping.schedule(order));
        System.out.println("v2: identical downstream flakiness, two different outcomes --");
        System.out.println("    Inventory's 3rd attempt succeeds; Shipping never gets a 3rd attempt.");
    }
}
