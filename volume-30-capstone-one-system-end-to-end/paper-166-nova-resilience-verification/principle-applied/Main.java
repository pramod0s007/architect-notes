/**
 * PRINCIPLE-APPLIED: instead of a one-time manual GameDay drill whose recorded result goes
 * stale the moment the call path it checked changes, the same failure-injection experiment is
 * wired into an automated chaos-experiment suite that re-runs it against whichever call path
 * implementation is selected for a given modeled deploy -- not once, manually, against a fixed
 * point in time. In this model, "Deploy 1" selects the same callDownstreamCorrect call path the
 * other two scenarios use, and "Deploy 2" selects the same callDownstreamRefactored call path
 * the other two scenarios use, so the experiment is genuinely exercising the same call-path
 * code, not a boolean flag standing in for it. Because the suite re-runs the identical
 * experiment against each modeled deploy's own selected call path, the regression introduced by
 * selecting the refactored call path in Deploy 2 is reported by that same deploy's own suite
 * run, rather than depending on a drill result recorded at an earlier point in the model.
 */
public class Main {

    static final int FAILURE_THRESHOLD = 3;

    /** The call-path shape the chaos suite exercises -- shared with the pressure/wrong-direction scenarios. */
    interface CallPath {
        void call(CircuitBreaker breaker, boolean simulateFailure);
    }

    public static void main(String[] args) {
        System.out.println("-- Nova inventory-lookup circuit breaker, automated chaos suite on every modeled deploy --");
        System.out.println("Failure threshold: " + FAILURE_THRESHOLD + " routed failures opens the breaker");
        System.out.println();

        // Deploy 1: this modeled deploy's call path is the pre-refactor call path.
        System.out.println("Deploy 1 -- chaos suite runs the same experiment against this deploy's call path");
        System.out.println("(the pre-refactor call path, which routes simulated failures to the breaker):");
        ChaosResult deploy1 = runChaosExperiment(Main::callDownstreamCorrect);
        System.out.println("  Chaos suite result: " + deploy1);
        System.out.println();

        // Deploy 2: this modeled deploy's call path is the refactored call path from the other two scenarios.
        System.out.println("Deploy 2 -- this deploy's call path is the refactored call path (does not route");
        System.out.println("simulated failures to the breaker). The chaos suite re-runs the same experiment,");
        System.out.println("automatically, against this deploy's own call path:");
        ChaosResult deploy2 = runChaosExperiment(Main::callDownstreamRefactored);
        System.out.println("  Chaos suite result: " + deploy2);
        System.out.println();

        System.out.println("Result: the regression in Deploy 2's call path is reported by that same deploy's");
        System.out.println("own chaos suite run, because the suite re-executed the call path this deploy actually");
        System.out.println("selected -- it did not reuse a result recorded against an earlier deploy's call path.");
    }

    enum ChaosResult { PASS, FAIL }

    /**
     * Runs the same failure-injection experiment the GameDay drill runs manually, but against
     * whichever call path is passed in for this modeled deploy, and reports whether the breaker
     * is open after that many injected failures (PASS) or not (FAIL).
     */
    static ChaosResult runChaosExperiment(CallPath callPath) {
        CircuitBreaker breaker = new CircuitBreaker(FAILURE_THRESHOLD);
        for (int i = 0; i < FAILURE_THRESHOLD; i++) {
            callPath.call(breaker, true);
        }
        return breaker.getState() == CircuitBreaker.State.OPEN ? ChaosResult.PASS : ChaosResult.FAIL;
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

        State getState() {
            return state;
        }
    }
}
