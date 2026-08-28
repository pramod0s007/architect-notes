// Knows about all three extracted services directly.
class MobileAppClient {
    private final PaymentRouter paymentRouterEndpoint = new PaymentRouter();
    private final InventoryService inventoryServiceEndpoint = new InventoryService();
    private final ShippingService shippingServiceEndpoint = new ShippingService();

    String placeOrder(Order order) {
        boolean charged = paymentRouterEndpoint.charge(order);
        if (!charged) return "REJECTED: payment failed";

        boolean reserved = inventoryServiceEndpoint.reserve(order);
        if (!reserved) return "REJECTED: out of stock";

        String tracking = shippingServiceEndpoint.schedule(order);
        return "CONFIRMED: " + order.getId() + " tracking=" + tracking;
    }
}
