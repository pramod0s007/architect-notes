import java.util.ArrayList;
import java.util.List;

class OrderSaga {
    private final PaymentRouter paymentRouter = new PaymentRouter();
    private final InventoryService inventory = new InventoryService();
    private final ShippingService shipping = new ShippingService();

    String placeOrder(Order order) {
        List<Runnable> compensations = new ArrayList<>();

        if (!paymentRouter.charge(order)) {
            return "REJECTED: payment failed";
        }
        compensations.add(() -> paymentRouter.refund(order));

        if (!inventory.reserve(order)) {
            runCompensations(compensations);
            return "REJECTED: out of stock";
        }
        compensations.add(() -> inventory.release(order));

        String tracking = shipping.schedule(order);
        if (tracking == null) {
            runCompensations(compensations);
            return "FAILED: shipping unavailable, all prior steps rolled back";
        }
        return "CONFIRMED: " + tracking;
    }

    private void runCompensations(List<Runnable> compensations) {
        for (int i = compensations.size() - 1; i >= 0; i--) {
            compensations.get(i).run();
        }
    }

    PaymentRouter payments() { return paymentRouter; }
    InventoryService inventoryService() { return inventory; }
}
