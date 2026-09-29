/**
 * PRESSURE: two services were built with a circuit breaker mechanism, correctly configured, at
 * launch time. Neither has ever been re-verified since. An unrelated refactor of Service A's code
 * later removed its circuit breaker entirely. A separate unrelated refactor of Service B's code
 * later left its circuit breaker in place but silently changed its failure threshold from 5 to
 * 500 -- the class is still there, but its trigger point has moved far outside the fixed-count
 * failure workload this paper's later tests will exercise it with. Neither regression is
 * discovered, because nothing has ever checked.
 */
public class Main {
    private static final int ORIGINAL_THRESHOLD = 5;
    private static final int REGRESSED_THRESHOLD_SERVICE_B = 500;

    public static void main(String[] args) {
        System.out.println("Documented resilience mechanisms (as originally built and reviewed):");
        System.out.println("  Service A: CircuitBreaker present, threshold=" + ORIGINAL_THRESHOLD);
        System.out.println("  Service B: CircuitBreaker present, threshold=" + ORIGINAL_THRESHOLD);

        System.out.println();
        System.out.println("Check status: no resilience mechanism on either service has ever been re-verified "
                + "since it was originally built.");

        System.out.println();
        System.out.println("Service A actual current state: CircuitBreaker class removed entirely by an "
                + "unrelated refactor -- undiscovered");
        System.out.println("Service B actual current state: CircuitBreaker still present, but its failure "
                + "threshold was silently changed from " + ORIGINAL_THRESHOLD + " to "
                + REGRESSED_THRESHOLD_SERVICE_B + " -- undiscovered");

        System.out.println();
        System.out.println("Neither regression was ever discovered, because no resilience mechanism has ever "
                + "been re-checked since it was built -- both would fail silently in a real outage");
    }
}
