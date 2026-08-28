public class Main {
    public static void main(String[] args) {
        ShippingService shipping = new ShippingService();
        BrokenIdempotentShippingEventHandler handler = new BrokenIdempotentShippingEventHandler(shipping);
        Order order = new Order("ORD-1");
        OrderEvent event = new OrderEvent("EVT-1", order);

        String first = handler.handle(event);
        String second = handler.handle(event); // same eventId, redelivered

        System.out.println("wrong-direction: dedup code exists, but the key isn't the event's identity.");
        System.out.println("    first=" + first + ", second=" + second);
        System.out.println("    shipmentCount=" + shipping.shipmentCount(order));
        System.out.println("    The dedup check never matches anything -- every key is new.");
    }
}
