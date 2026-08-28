public class Main {
    public static void main(String[] args) {
        ShippingService shipping = new ShippingService();
        Order order = new Order("ORD-1");
        OrderEvent event = new OrderEvent("EVT-1", order);

        shipping.schedule(event.getOrder());

        System.out.println("v1: one event, delivered once, processed once.");
        System.out.println("    shipmentCount=" + shipping.shipmentCount(order));
    }
}
