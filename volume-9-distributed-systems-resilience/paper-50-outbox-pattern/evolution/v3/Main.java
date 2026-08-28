public class Main {
    public static void main(String[] args) {
        Outbox outbox = new Outbox();
        FlakyMessageBroker broker = new FlakyMessageBroker(2); // fails twice, then succeeds
        InventoryServiceWithOutbox inventory = new InventoryServiceWithOutbox(outbox);
        OutboxRelay relay = new OutboxRelay(outbox, broker);
        Order order = new Order("ORD-1");

        boolean reserved = inventory.reserve(order);
        System.out.println("v3: local write + outbox entry, same method -- reserved=" + reserved
                + ", isReserved=" + inventory.isReserved(order));
        System.out.println("    broker.receivedEvents() before any poll=" + broker.receivedEvents());

        for (int poll = 1; poll <= 3; poll++) {
            relay.pollOnce();
            System.out.println("    after poll " + poll + ": receivedEvents=" + broker.receivedEvents()
                    + ", outbox unpublished=" + outbox.unpublished().size());
        }

        System.out.println("    The event was never lost -- it sat in the outbox until the broker actually confirmed it.");
    }
}
