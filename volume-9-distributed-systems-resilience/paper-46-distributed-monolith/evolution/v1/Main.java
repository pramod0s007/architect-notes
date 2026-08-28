public class Main {
    public static void main(String[] args) {
        InventoryServiceWithOwnDb inventory = new InventoryServiceWithOwnDb();
        Order order = new Order("ORD-1");

        inventory.reserve(order);
        System.out.println("Reservation status: " + InventoryOwnedDatabase.statusOf(order.getId()));
        System.out.println("v1: this service owns its own data. No other service can be broken");
        System.out.println("    by a schema change here.");
    }
}
