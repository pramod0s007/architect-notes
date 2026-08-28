class OrderSagaNaive {
    private final PaymentRouter paymentRouter = new PaymentRouter();
    private final InventoryService inventory = new InventoryService();
    private final ShippingService shipping = new ShippingService();

    String placeOrder(Order order) {
        if (!paymentRouter.charge(order)) {
            return "REJECTED: payment failed";
        }
        if (!inventory.reserve(order)) {
            return "REJECTED: out of stock";
        }
        String tracking = shipping.schedule(order);
        if (tracking == null) {
            return "FAILED: shipping unavailable"; // nothing undoes payment or inventory
        }
        return "CONFIRMED: " + tracking;
    }

    PaymentRouter payments() { return paymentRouter; }
    InventoryService inventoryService() { return inventory; }
}
