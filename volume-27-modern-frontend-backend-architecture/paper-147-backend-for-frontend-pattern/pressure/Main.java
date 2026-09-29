/**
 * PRESSURE: a client renders one screen by calling three backend microservices directly, one
 * after another. User and Inventory succeed, but Orders is down. Because the client's logic aborts
 * on the first failure, Inventory is never even called, and the User data that DID succeed is
 * discarded along with everything else -- the whole screen fails to render, even though 2 of 3
 * calls it needed would have succeeded if attempted.
 */
public class Main {
    private static final int USER_SERVICE_LATENCY_MS = 80;
    private static final int ORDERS_SERVICE_LATENCY_MS = 150;
    private static final int INVENTORY_SERVICE_LATENCY_MS = 120;

    public static void main(String[] args) {
        boolean userSucceeded = true;
        boolean ordersSucceeded = false; // Orders service is down this run
        int elapsedMs = 0;

        System.out.println("Sequential client-side calls to render one screen:");

        elapsedMs += USER_SERVICE_LATENCY_MS;
        System.out.println("  User service: " + (userSucceeded ? "succeeded" : "FAILED")
                + " (" + USER_SERVICE_LATENCY_MS + "ms, elapsed " + elapsedMs + "ms)");

        elapsedMs += ORDERS_SERVICE_LATENCY_MS;
        System.out.println("  Orders service: " + (ordersSucceeded ? "succeeded" : "FAILED")
                + " (" + ORDERS_SERVICE_LATENCY_MS + "ms, elapsed " + elapsedMs + "ms)");

        if (!ordersSucceeded) {
            System.out.println("  Inventory service: never called -- client aborted on first failure");
            System.out.println();
            System.out.println("Result: page render FAILS. Total time spent before failure: " + elapsedMs
                    + "ms. The already-fetched User data is discarded along with everything else.");
            return;
        }

        elapsedMs += INVENTORY_SERVICE_LATENCY_MS;
        System.out.println("  Inventory service: succeeded (" + INVENTORY_SERVICE_LATENCY_MS + "ms, elapsed "
                + elapsedMs + "ms)");
        System.out.println();
        System.out.println("Result: page render succeeds. Total time: " + elapsedMs + "ms.");
    }
}
