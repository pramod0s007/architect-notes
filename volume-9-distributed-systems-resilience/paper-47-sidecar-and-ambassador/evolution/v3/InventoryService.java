class InventoryService {
    private final FlakyBackend backend = new FlakyBackend(2); // fails twice, then succeeds
    private final ResilienceSidecar sidecar = new ResilienceSidecar(3, 50);

    boolean reserve(Order order) {
        return sidecar.callWithRetry(() -> callInventoryBackend(order), false);
    }

    private boolean callInventoryBackend(Order order) throws TimeoutException {
        return backend.call();
    }
}
