public class Main {
    public static void main(String[] args) {
        ShippingService shipping = new ShippingService();
        IdempotentShippingEventHandler handler = new IdempotentShippingEventHandler(shipping);
        Order order = new Order("ORD-1");
        OrderEvent event = new OrderEvent("EVT-1", order);

        String first = handler.handle(event);
        String second = handler.handle(event); // same eventId, redelivered

        System.out.println("v3: the SAME event, delivered twice, processed once.");
        System.out.println("    first=" + first + ", second=" + second);
        System.out.println("    shipmentCount=" + shipping.shipmentCount(order));
    }
}
