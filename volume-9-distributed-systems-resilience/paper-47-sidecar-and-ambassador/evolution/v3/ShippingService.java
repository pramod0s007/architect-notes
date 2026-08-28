class ShippingService {
    private final FlakyBackend backend = new FlakyBackend(2); // SAME flakiness as Inventory
    private final ResilienceSidecar sidecar = new ResilienceSidecar(3, 50); // SAME policy, same class

    String schedule(Order order) {
        boolean ok = sidecar.callWithRetry(() -> callShippingBackend(order), false);
        return ok ? "TRK-" + order.getId() : "SCHEDULING_FAILED";
    }

    private boolean callShippingBackend(Order order) throws TimeoutException {
        return backend.call();
    }
}
