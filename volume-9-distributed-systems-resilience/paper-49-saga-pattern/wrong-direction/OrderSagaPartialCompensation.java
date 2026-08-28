// Wrong direction: compensation was added, but hand-written per branch instead
// of accumulated generically. Whoever wrote the shipping-failure branch copied
// the "undo the last thing" idea and forgot payment was ALSO a prior step.
class OrderSagaPartialCompensation {
    private final PaymentRouter paymentRouter = new PaymentRouter();
    private final InventoryService inventory = new InventoryService();
    private final ShippingService shipping = new ShippingService();

    String placeOrder(Order order) {
        if (!paymentRouter.charge(order)) {
            return "REJECTED: payment failed";
        }

        if (!inventory.reserve(order)) {
            paymentRouter.refund(order); // compensates payment -- fine, so far
            return "REJECTED: out of stock";
        }

        String tracking = shipping.schedule(order);
        if (tracking == null) {
            inventory.release(order); // undoes inventory -- forgets payment entirely
            return "FAILED: shipping unavailable";
        }
        return "CONFIRMED: " + tracking;
    }

    PaymentRouter payments() { return paymentRouter; }
    InventoryService inventoryService() { return inventory; }
}
