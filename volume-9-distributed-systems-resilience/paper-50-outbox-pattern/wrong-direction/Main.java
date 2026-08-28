public class Main {
    public static void main(String[] args) {
        Outbox outbox = new Outbox();
        FlakyMessageBroker broker = new FlakyMessageBroker(2); // fails twice, then succeeds
        InventoryServiceWithOutbox inventory = new InventoryServiceWithOutbox(outbox);
        OutboxRelayBroken relay = new OutboxRelayBroken(outbox, broker);
        Order order = new Order("ORD-1");

        inventory.reserve(order);
        relay.pollOnce(); // the broker is down on this first attempt

        System.out.println("wrong-direction: relay marks published=true before confirming the broker got it.");
        System.out.println("    outbox entry published=" + outbox.all().get(0).published + " (marked done)");
        System.out.println("    broker.receivedEvents()=" + broker.receivedEvents() + " (never actually received)");
        System.out.println("    Even after 2 more polls: " );
        relay.pollOnce();
        relay.pollOnce();
        System.out.println("    broker.receivedEvents()=" + broker.receivedEvents()
                + " -- the relay never retries an entry it already marked published.");
    }
}
