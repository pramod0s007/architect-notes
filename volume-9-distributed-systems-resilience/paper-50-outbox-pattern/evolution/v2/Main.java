public class Main {
    public static void main(String[] args) {
        MessageBroker broker = new MessageBroker();
        broker.goDown(); // the broker is unreachable when this order is reserved

        InventoryServiceDualWrite inventory = new InventoryServiceDualWrite(broker);
        Order order = new Order("ORD-1");

        boolean reserved = inventory.reserve(order);

        System.out.println("v2: local write and event publish are two separate, uncoordinated steps.");
        System.out.println("    reserved=" + reserved + ", isReserved=" + inventory.isReserved(order));
        System.out.println("    broker.receivedEvents()=" + broker.receivedEvents());
        System.out.println("    Local state says RESERVED. Shipping was never told -- the event is gone.");
    }
}
