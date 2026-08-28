public class Main {
    public static void main(String[] args) {
        InventoryService inventory = new InventoryService();
        Order order = new Order("ORD-1");

        boolean reserved = inventory.reserve(order);
        System.out.println("v1: one service, one local write, nothing downstream to notify yet.");
        System.out.println("    reserved=" + reserved + ", isReserved=" + inventory.isReserved(order));
    }
}
