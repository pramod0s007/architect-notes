/**
 * PRESSURE: Nova's inventory-lookup circuit breaker (guarding calls to a modeled downstream
 * dependency) is checked once, before a refactor, and confirmed to open after 3 simulated
 * failure events routed to it. That single check is then treated as settled going forward --
 * nothing re-runs it after later changes to the call path. A later refactor adds a fallback
 * branch but, in doing so, stops routing the simulated failure to the breaker's own failure
 * counter. From that point on, every simulated failure event can occur and the breaker's own
 * recorded state never reflects it, with nothing in this model re-checking to notice.
 */
public class Main {

    static final int FAILURE_THRESHOLD = 3;

    public static void main(String[] args) {
        CircuitBreaker breaker = new CircuitBreaker(FAILURE_THRESHOLD);

        System.out.println("-- Nova inventory-lookup circuit breaker, checked once, not re-checked after a refactor --");
        System.out.println("Failure threshold: " + FAILURE_THRESHOLD + " routed failures opens the breaker");
        System.out.println();

        // Before the refactor: the call path routes every simulated failure event to the breaker.
        System.out.println("Before the refactor -- call path routes every simulated failure event to the breaker:");
        for (int i = 1; i <= FAILURE_THRESHOLD; i++) {
            callDownstreamCorrect(breaker, true);
            System.out.println("  Simulated failure " + i + " routed to breaker. State: " + breaker.getState());
        }
        System.out.println("This pre-refactor check confirms: the breaker opens after " + FAILURE_THRESHOLD
                + " routed failures, for this call path, at this point in time.");
        System.out.println();

        // A later refactor adds a fallback branch, but that branch never calls breaker.recordFailure().
        System.out.println("After a later refactor -- a fallback branch is added to the call path, but the");
        System.out.println("fallback branch does not route the simulated failure to the breaker's own counter.");
        System.out.println("Nothing re-runs the pre-refactor check above against this new call path:");
        breaker.reset();
        for (int i = 1; i <= 5; i++) {
            callDownstreamRefactored(breaker, true);
            System.out.println("  Simulated failure " + i + "; refactored call path did not route it. State: "
                    + breaker.getState());
        }
        System.out.println();

        System.out.println("Result: breaker state after 5 simulated failure events, post-refactor = " + breaker.getState());
        System.out.println("The pre-refactor check above is never re-run against this new call path -- the refactor");
        System.out.println("ships without anything in this model re-verifying whether the breaker still opens.");
    }

    /** Pre-refactor call path: every simulated failure event is routed to the breaker. */
    static void callDownstreamCorrect(CircuitBreaker breaker, boolean simulateFailure) {
        if (simulateFailure) {
            breaker.recordFailure();
        } else {
            breaker.recordSuccess();
        }
    }

    /**
     * Post-refactor call path: a fallback branch was added, but it returns without ever calling
     * breaker.recordFailure() -- the breaker's failure counter no longer reflects the simulated
     * failure events this call path is given.
     */
    static void callDownstreamRefactored(CircuitBreaker breaker, boolean simulateFailure) {
        if (simulateFailure) {
            // Fallback branch: does not route the simulated failure event to the breaker.
            return;
        }
        breaker.recordSuccess();
    }

    /** A minimal consecutive-failure circuit breaker. */
    static class CircuitBreaker {
        enum State { CLOSED, OPEN }

        private final int failureThreshold;
        private int consecutiveFailures = 0;
        private State state = State.CLOSED;

        CircuitBreaker(int failureThreshold) {
            this.failureThreshold = failureThreshold;
        }

        void recordFailure() {
            consecutiveFailures++;
            if (consecutiveFailures >= failureThreshold) {
                state = State.OPEN;
            }
        }

        void recordSuccess() {
            consecutiveFailures = 0;
        }

        void reset() {
            consecutiveFailures = 0;
            state = State.CLOSED;
        }

        State getState() {
            return state;
        }
    }
}
