/**
 * Decorator implementing the Circuit Breaker pattern.
 *
 * States:
 *   CLOSED  — requests pass through normally
 *   OPEN    — requests are short-circuited (immediately return 503) after
 *             {@code failureThreshold} consecutive failures
 *   HALF-OPEN — after {@code resetTimeoutMs} the next request is allowed
 *             through as a probe; if it succeeds the circuit closes, if it
 *             fails the circuit re-opens
 */
public class CircuitBreakerDecorator implements HttpClient {

    public enum State { CLOSED, OPEN, HALF_OPEN }

    private final HttpClient delegate;
    private final int failureThreshold;
    private final long resetTimeoutMs;

    private State state           = State.CLOSED;
    private int   consecutiveFails = 0;
    private long  openedAt         = 0;

    public CircuitBreakerDecorator(HttpClient delegate, int failureThreshold, long resetTimeoutMs) {
        this.delegate         = delegate;
        this.failureThreshold = failureThreshold;
        this.resetTimeoutMs   = resetTimeoutMs;
    }

    @Override
    public Response get(String url) {
        return execute("GET", url, null);
    }

    @Override
    public Response post(String url, String body) {
        return execute("POST", url, body);
    }

    private synchronized Response execute(String method, String url, String body) {
        transitionIfNeeded();

        if (state == State.OPEN) {
            System.out.println("  [CircuitBreaker] OPEN — short-circuiting " + method + " " + url);
            return new Response(503, "Circuit breaker is OPEN — request blocked");
        }

        if (state == State.HALF_OPEN) {
            System.out.println("  [CircuitBreaker] HALF-OPEN — probing with " + method + " " + url);
        }

        Response response = "POST".equals(method)
                ? delegate.post(url, body)
                : delegate.get(url);

        recordOutcome(response);
        return response;
    }

    private void transitionIfNeeded() {
        if (state == State.OPEN) {
            long elapsed = System.currentTimeMillis() - openedAt;
            if (elapsed >= resetTimeoutMs) {
                System.out.println("  [CircuitBreaker] -> HALF-OPEN after " + elapsed + "ms");
                state = State.HALF_OPEN;
            }
        }
    }

    private void recordOutcome(Response response) {
        if (response.isServerError()) {
            consecutiveFails++;
            System.out.println("  [CircuitBreaker] Failure #" + consecutiveFails
                    + " (threshold=" + failureThreshold + ")");
            if (consecutiveFails >= failureThreshold) {
                state    = State.OPEN;
                openedAt = System.currentTimeMillis();
                System.out.println("  [CircuitBreaker] -> OPEN (tripped after "
                        + consecutiveFails + " consecutive failures)");
            }
        } else {
            if (state == State.HALF_OPEN) {
                System.out.println("  [CircuitBreaker] Probe succeeded -> CLOSED");
            }
            state           = State.CLOSED;
            consecutiveFails = 0;
        }
    }

    public State getState() { return state; }
}
