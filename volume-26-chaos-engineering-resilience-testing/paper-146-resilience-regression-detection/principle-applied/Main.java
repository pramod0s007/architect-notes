/**
 * PRINCIPLE APPLIED: instead of checking whether a CircuitBreaker class merely exists, an
 * automated resilience-assertion test exercises each mechanism's actual triggering condition --
 * it feeds a fixed number of consecutive failures through the mechanism and asserts it actually
 * opens by the originally-specified threshold. This catches both regression types: Service A has
 * no mechanism at all to exercise, so its presence assertion fails immediately; Service B's
 * mechanism exists and runs, but its regressed threshold means it never opens within the failures
 * the test feeds it, so its behavioral assertion fails even though the class itself is present.
 * If either assertion fails, the test throws, exiting non-zero -- an assertion tool a checklist
 * has no equivalent of.
 */
public class Main {
    private static final int ORIGINAL_THRESHOLD = 5;
    private static final int TEST_FAILURES = 10;

    public static void main(String[] args) {
        boolean serviceACircuitBreakerPresent = false;
        CircuitBreaker serviceBBreaker = new CircuitBreaker(500);

        System.out.println("Automated resilience-assertion test: feed " + TEST_FAILURES + " consecutive "
                + "failures, assert the circuit breaker opens by the " + ORIGINAL_THRESHOLD + "th failure.");

        System.out.println();
        if (!serviceACircuitBreakerPresent) {
            System.out.println("Service A: FAIL -- no circuit breaker mechanism exists to test at all "
                    + "(removed entirely)");
        }

        int tripPoint = -1;
        for (int i = 1; i <= TEST_FAILURES; i++) {
            serviceBBreaker.recordFailure();
            if (serviceBBreaker.isOpen() && tripPoint == -1) {
                tripPoint = i;
            }
        }
        String tripDescription = (tripPoint == -1)
                ? "never (within " + TEST_FAILURES + " failures)"
                : "failure #" + tripPoint;
        System.out.println("Service B: circuit breaker opened at " + tripDescription + ", expected to open "
                + "by failure #" + ORIGINAL_THRESHOLD);
        boolean serviceBPassed = tripPoint != -1 && tripPoint <= ORIGINAL_THRESHOLD;
        System.out.println("Service B: " + (serviceBPassed ? "PASS" : "FAIL")
                + " -- circuit breaker did not open within the expected threshold");

        System.out.println();
        System.out.println("Both regressions caught: Service A's complete removal and Service B's silently "
                + "regressed threshold -- because the test exercises the actual triggering condition, not "
                + "just the presence of the class");

        boolean serviceAPassed = false;
        if (!serviceAPassed || !serviceBPassed) {
            throw new AssertionError("resilience regression test failed -- see printed output above for "
                    + "which service(s) failed and why");
        }
    }

    private static class CircuitBreaker {
        private final int threshold;
        private int consecutiveFailures = 0;
        private boolean open = false;

        CircuitBreaker(int threshold) {
            this.threshold = threshold;
        }

        void recordFailure() {
            consecutiveFailures++;
            if (consecutiveFailures >= threshold) {
                open = true;
            }
        }

        boolean isOpen() {
            return open;
        }
    }
}
