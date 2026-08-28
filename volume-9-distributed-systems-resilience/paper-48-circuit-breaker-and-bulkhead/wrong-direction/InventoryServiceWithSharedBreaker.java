// Wrong direction: this reuses the SAME CircuitBreaker instance the payment
// path uses, instead of its own. "One shared class, configured the same way"
// (Paper 47's lesson) was meant for identical dependencies -- not for two
// completely unrelated ones sharing the same mutable failure count.
class InventoryServiceWithSharedBreaker {
    private final CountingInventoryBackend backend = new CountingInventoryBackend();
    private final CircuitBreaker breaker; // shared with PaymentRouterWithBreaker

    InventoryServiceWithSharedBreaker(CircuitBreaker breaker) {
        this.breaker = breaker;
    }

    boolean reserve(Order order) {
        if (!breaker.allowCall()) {
            return false; // short-circuited by PAYMENT's failures, not inventory's
        }
        boolean result = backend.reserve(order);
        breaker.recordSuccess();
        return result;
    }

    int backendInvocationCount() {
        return backend.invocationCount();
    }
}
