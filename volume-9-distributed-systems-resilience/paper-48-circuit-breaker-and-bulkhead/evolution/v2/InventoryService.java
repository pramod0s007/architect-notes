class InventoryService {
    private final HealthyInventoryBackend backend = new HealthyInventoryBackend();

    boolean reserve(Order order) {
        return backend.reserve(order);
    }
}
