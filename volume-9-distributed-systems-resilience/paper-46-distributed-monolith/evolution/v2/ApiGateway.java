class ApiGateway {
    private final PaymentRouter paymentRouter = new PaymentRouter();
    private final InventoryService inventoryService = new InventoryService();
    private final ShippingService shippingService = new ShippingService();

    String placeOrder(Order order) {
        if (!paymentRouter.charge(order)) return "REJECTED: payment failed";
        boolean reserved = inventoryService.reserve(order); // blocks here for 3s under load
        if (!reserved) return "REJECTED: out of stock";
        String tracking = shippingService.schedule(order); // never even starts until reserve() returns
        return "CONFIRMED: " + order.getId() + " tracking=" + tracking;
    }
}
