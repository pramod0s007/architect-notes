class PaymentRouterWithBreaker {
    private final DeadPaymentBackend backend = new DeadPaymentBackend();
    private final ResilienceSidecar sidecar = new ResilienceSidecar(3, 50);
    private final CircuitBreaker breaker;

    PaymentRouterWithBreaker(CircuitBreaker breaker) {
        this.breaker = breaker;
    }

    boolean charge(Order order) {
        if (!breaker.allowCall()) {
            return false; // breaker is open -- fail fast, never touch the dead backend
        }
        boolean result = sidecar.callWithRetry(() -> backend.charge(order), false);
        if (result) {
            breaker.recordSuccess();
        } else {
            breaker.recordFailure();
        }
        return result;
    }
}
