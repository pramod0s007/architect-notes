class ShippingService {
    private final FlakyBackend backend = new FlakyBackend(2); // fails twice, then succeeds -- SAME flakiness as Inventory

    String schedule(Order order) {
        int attempts = 0;
        while (attempts < 2) { // different retry count — nobody coordinated this
            try {
                boolean ok = callShippingBackend(order);
                return ok ? "TRK-" + order.getId() : "SCHEDULING_FAILED";
            } catch (TimeoutException e) {
                attempts++;
                // no delay between retries — nobody coordinated this either
            }
        }
        return "SCHEDULING_FAILED";
    }

    private boolean callShippingBackend(Order order) throws TimeoutException {
        return backend.call();
    }
}
