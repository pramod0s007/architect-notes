/**
 * PRINCIPLE APPLIED: same parallel BFF fan-out as the wrong-direction scenario, but the aggregation
 * logic treats each downstream call's outcome independently instead of all-or-nothing. A failed
 * call contributes an explicit "section unavailable" placeholder instead of failing the entire
 * response -- the client still gets a page, just with one section degraded instead of all three
 * missing.
 *
 * (Same latency-modeling simplification as the wrong-direction scenario: parallel dispatch is
 * represented as latency = max(latencies), not real concurrent execution.)
 */
public class Main {
    private static final int USER_SERVICE_LATENCY_MS = 80;
    private static final int ORDERS_SERVICE_LATENCY_MS = 150;
    private static final int INVENTORY_SERVICE_LATENCY_MS = 120;

    public static void main(String[] args) {
        boolean userSucceeded = true;
        boolean ordersSucceeded = false; // Orders service is down this run
        boolean inventorySucceeded = true;

        int parallelLatencyMs = Math.max(USER_SERVICE_LATENCY_MS,
                Math.max(ORDERS_SERVICE_LATENCY_MS, INVENTORY_SERVICE_LATENCY_MS));

        System.out.println("Parallel BFF calls (latency bounded by the slowest call, " + parallelLatencyMs + "ms):");
        System.out.println("  User service: " + (userSucceeded ? "succeeded" : "FAILED"));
        System.out.println("  Orders service: " + (ordersSucceeded ? "succeeded" : "FAILED"));
        System.out.println("  Inventory service: " + (inventorySucceeded ? "succeeded" : "FAILED"));

        System.out.println();
        System.out.println("Per-call graceful degradation: each result is aggregated independently.");
        System.out.println("Aggregated response:");
        System.out.println("  user section: " + (userSucceeded ? "populated" : "unavailable"));
        System.out.println("  orders section: " + (ordersSucceeded ? "populated" : "unavailable"));
        System.out.println("  inventory section: " + (inventorySucceeded ? "populated" : "unavailable"));

        int populatedSections = (userSucceeded ? 1 : 0) + (ordersSucceeded ? 1 : 0) + (inventorySucceeded ? 1 : 0);
        System.out.println();
        System.out.println("Result: page renders with " + populatedSections + " of 3 sections populated, instead "
                + "of failing entirely.");
    }
}
