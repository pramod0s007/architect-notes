class CircuitBreaker {
    private enum State { CLOSED, OPEN, HALF_OPEN }

    private final int failureThreshold;
    private final long cooldownMs;

    private State state = State.CLOSED;
    private int consecutiveFailures = 0;
    private long openedAt = 0;

    CircuitBreaker(int failureThreshold, long cooldownMs) {
        this.failureThreshold = failureThreshold;
        this.cooldownMs = cooldownMs;
    }

    synchronized boolean allowCall() {
        if (state == State.OPEN) {
            if (System.currentTimeMillis() - openedAt >= cooldownMs) {
                state = State.HALF_OPEN; // one trial call allowed
                return true;
            }
            return false; // still open -- fail fast, don't touch the backend
        }
        return true; // CLOSED or HALF_OPEN trial in progress
    }

    synchronized void recordSuccess() {
        consecutiveFailures = 0;
        state = State.CLOSED;
    }

    synchronized void recordFailure() {
        consecutiveFailures++;
        if (state == State.HALF_OPEN || consecutiveFailures >= failureThreshold) {
            state = State.OPEN;
            openedAt = System.currentTimeMillis();
        }
    }
}
