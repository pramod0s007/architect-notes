class SinglePaymentService {
    private final FlakyBackend backend = new FlakyBackend(1); // fails once, then succeeds

    boolean charge(Order order) {
        try {
            return callPaymentBackend(order);
        } catch (TimeoutException e) {
            try {
                return callPaymentBackend(order); // one retry, inline, fine for one service
            } catch (TimeoutException e2) {
                return false;
            }
        }
    }

    private boolean callPaymentBackend(Order order) throws TimeoutException {
        return backend.call();
    }
}
