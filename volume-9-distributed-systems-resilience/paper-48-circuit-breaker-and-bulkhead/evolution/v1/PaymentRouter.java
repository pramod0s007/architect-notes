class PaymentRouter {
    private final DeadPaymentBackend backend = new DeadPaymentBackend();
    private final ResilienceSidecar sidecar = new ResilienceSidecar(3, 50);

    boolean charge(Order order) {
        return sidecar.callWithRetry(() -> backend.charge(order), false);
    }
}
