// The SAME three-call sequence, written independently by a different team.
class WebAppClient {
    private final PaymentRouter paymentRouterEndpoint = new PaymentRouter();
    private final InventoryService inventoryServiceEndpoint = new InventoryService();
    private final ShippingService shippingServiceEndpoint = new ShippingService();

    String placeOrder(Order order) {
        if (!paymentRouterEndpoint.charge(order)) return "Payment declined";
        if (!inventoryServiceEndpoint.reserve(order)) return "Out of stock";
        return "Order placed: " + shippingServiceEndpoint.schedule(order);
    }
}
