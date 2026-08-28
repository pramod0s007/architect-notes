class ShippingService {
    private final HealthyShippingBackend backend = new HealthyShippingBackend();

    String schedule(Order order) {
        return backend.schedule(order);
    }
}
