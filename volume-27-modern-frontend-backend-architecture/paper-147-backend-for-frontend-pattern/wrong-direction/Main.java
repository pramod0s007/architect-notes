/**
 * WRONG DIRECTION: a Backend-for-Frontend service now fetches all three downstream services in
 * parallel instead of sequentially, cutting latency to the slowest single call instead of the sum
 * of all three. But its aggregation logic is still all-or-nothing: if any one downstream call
 * fails, the BFF discards every result -- including the ones that succeeded -- and returns a
 * single overall failure to the client. Faster, but the same "one failure kills the whole page"
 * problem as the pressure scenario.
 *
 * (This model represents parallel dispatch as latency = max(latencies) for illustration, not as
 * real concurrent execution -- a real parallel fan-out has its own dispatch/join overhead and
 * network variability beyond what this simplification captures.)
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

        boolean anyFailed = !userSucceeded || !ordersSucceeded || !inventorySucceeded;
        System.out.println();
        if (anyFailed) {
            System.out.println("Naive aggregation: any downstream failure means the whole response fails.");
            System.out.println("Result: client receives nothing -- User and Inventory data is discarded even "
                    + "though 2 of 3 calls succeeded.");
        } else {
            System.out.println("Result: page render succeeds.");
        }
    }
}
