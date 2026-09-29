/**
 * WRONG-DIRECTION: instead of never re-checking the breaker after it's built (the pressure
 * scenario), a manual GameDay chaos drill is run once: in this modeled run, 3 simulated failure
 * events are used to confirm the breaker opens, and the drill's result is recorded for that
 * run. This closes the specific gap the pressure scenario demonstrates for the call path that
 * exists at the moment the drill runs -- the check is genuine, not just assumed. But the drill
 * is a one-time, manually-triggered event in this model. The same refactor that stops routing
 * simulated failures to the breaker's counter happens after the drill already ran, and nothing
 * re-runs the drill to catch it -- the GameDay's recorded result keeps being treated as still
 * describing the breaker's current behavior, even though the call path it checked has changed.
 */
public class Main {

    static final int FAILURE_THRESHOLD = 3;

    public static void main(String[] args) {
        CircuitBreaker breaker = new CircuitBreaker(FAILURE_THRESHOLD);

        System.out.println("-- Nova inventory-lookup circuit breaker, one-time manual GameDay drill --");
        System.out.println("Failure threshold: " + FAILURE_THRESHOLD + " routed failures opens the breaker");
        System.out.println();

        // GameDay drill, run once, against the call path as it exists at this point in the model.
        System.out.println("GameDay drill (manual, one-time, run against the call path at this point):");
        for (int i = 1; i <= FAILURE_THRESHOLD; i++) {
            callDownstreamCorrect(breaker, true);
            System.out.println("  Simulated failure " + i + " routed to breaker. State: " + breaker.getState());
        }
        String gameDayResult = breaker.getState() == CircuitBreaker.State.OPEN ? "PASS" : "FAIL";
        System.out.println("GameDay result recorded for this run: " + gameDayResult);
        System.out.println();

        // Later in the model, the call path is refactored. Nothing re-runs the drill.
        System.out.println("Later in this model, the call path is refactored (a fallback branch is added that");
        System.out.println("does not route the simulated failure to the breaker's own counter). The GameDay");
        System.out.println("drill is not re-run against this refactored call path -- its recorded result is");
        System.out.println("treated as still describing the breaker's current behavior:");
        breaker.reset();
        for (int i = 1; i <= 5; i++) {
            callDownstreamRefactored(breaker, true);
            System.out.println("  Simulated failure " + i + "; refactored call path did not route it. State: "
                    + breaker.getState());
        }
        System.out.println();

        System.out.println("Result: breaker state after 5 simulated failure events, post-refactor = "
                + breaker.getState());
        System.out.println("The GameDay drill's recorded result (" + gameDayResult + ") described the call path as it");
        System.out.println("existed when the drill ran. It did not, and could not, describe the call path");
        System.out.println("after the later refactor, because the drill was never re-run against that code.");
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
