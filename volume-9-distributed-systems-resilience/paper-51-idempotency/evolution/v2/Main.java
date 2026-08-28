public class Main {
    public static void main(String[] args) {
        ShippingService shipping = new ShippingService();
        ShippingEventHandler handler = new ShippingEventHandler(shipping);
        Order order = new Order("ORD-1");
        OrderEvent event = new OrderEvent("EVT-1", order);

        handler.handle(event); // first delivery
        handler.handle(event); // redelivery -- the relay never got an ack the first time

        System.out.println("v2: the SAME event, delivered twice, processed twice.");
        System.out.println("    shipmentCount=" + shipping.shipmentCount(order));
        System.out.println("    One reservation became two physical shipments.");
    }
}
