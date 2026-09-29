/**
 * WRONG DIRECTION: after the pressure scenario's undiscovered drift, the team adds a manual
 * code-review checklist item: "does a CircuitBreaker class still exist in this service's code?"
 * This catches Service A's regression -- the class is simply gone, so the checklist correctly
 * flags it. But the checklist only checks for the class's presence, not what value it's
 * configured with, so Service B's silently-changed threshold passes the checklist even though
 * the breaker's trigger point has moved far outside the failure workload it's actually meant to
 * protect against.
 */
public class Main {
    private static final int ORIGINAL_THRESHOLD = 5;

    public static void main(String[] args) {
        boolean serviceACircuitBreakerPresent = false;
        boolean serviceBCircuitBreakerPresent = true;

        System.out.println("Manual code-review checklist: does a CircuitBreaker class exist in this "
                + "service's code?");

        System.out.println();
        System.out.println("Service A: CircuitBreaker present? " + serviceACircuitBreakerPresent + " -- "
                + (serviceACircuitBreakerPresent ? "PASS" : "FAIL, flagged for removal"));
        System.out.println("Service B: CircuitBreaker present? " + serviceBCircuitBreakerPresent + " -- "
                + (serviceBCircuitBreakerPresent ? "PASS" : "FAIL, flagged for removal"));

        System.out.println();
        System.out.println("Service B's actual threshold was silently changed from " + ORIGINAL_THRESHOLD
                + " to 500 by an unrelated refactor -- the checklist only checks whether the class exists, "
                + "not what value it's configured with, so this passes even though the breaker's trigger "
                + "point has moved far outside the failure workload it's actually meant to protect against");
    }
}
