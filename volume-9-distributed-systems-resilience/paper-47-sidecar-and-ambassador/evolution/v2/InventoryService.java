class InventoryService {
    private final FlakyBackend backend = new FlakyBackend(2); // fails twice, then succeeds

    boolean reserve(Order order) {
        int attempts = 0;
        while (attempts < 3) {
            try {
                return callInventoryBackend(order);
            } catch (TimeoutException e) {
                attempts++;
            }
        }
        return false;
    }

    private boolean callInventoryBackend(Order order) throws TimeoutException {
        return backend.call();
    }
}
