class InventoryService {
    static volatile boolean simulateLoad = false;

    boolean reserve(Order order) {
        if (simulateLoad) {
            try {
                Thread.sleep(3000); // a realistic stand-in for a slow query under load
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        // still reads/writes the SAME orders table the payment step just wrote to
        SharedOrderDatabase.execute("inventory_status", order.getId(), "RESERVED");
        return true;
    }
}
