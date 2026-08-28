class HealthyShippingBackend {
    String schedule(Order order) {
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return "TRK-" + order.getId();
    }
}
