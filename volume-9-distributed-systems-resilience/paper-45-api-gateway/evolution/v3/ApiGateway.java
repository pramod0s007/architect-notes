class ApiGateway {
    private final PaymentRouter paymentRouter = new PaymentRouter();
    private final InventoryService inventoryService = new InventoryService();
    private final ShippingService shippingService = new ShippingService();

    String placeOrder(Order order) {
        if (!paymentRouter.charge(order)) return "REJECTED: payment failed";
        if (!inventoryService.reserve(order)) return "REJECTED: out of stock";
        String tracking = shippingService.schedule(order);
        return "CONFIRMED: " + order.getId() + " tracking=" + tracking;
    }
}
