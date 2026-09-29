/**
 * PRESSURE: a team wants to verify that the order-processing system tolerates an inventory-
 * service outage. They inject a fault (100% failure) directly into the inventory-service call
 * path with no scoping at all -- every request in the system routes through the faulted call,
 * because nothing in this version of the experiment separates "traffic under test" from
 * "all production traffic." There is also no abort condition: once started, the experiment
 * runs for its full planned duration regardless of what happens.
 */
public class Main {
    private static final int TOTAL_TICKS = 30;
    private static final int REQUESTS_PER_TICK = 100;

    public static void main(String[] args) {
        long totalRequests = 0;
        long totalFailed = 0;

        for (int tick = 1; tick <= TOTAL_TICKS; tick++) {
            int requestsThisTick = REQUESTS_PER_TICK;
            // Fault injected with no scoping: every single request in the system is routed
            // through the faulted inventory-service call, so every request fails.
            int failedThisTick = requestsThisTick;

            totalRequests += requestsThisTick;
            totalFailed += failedThisTick;

            if (tick == 1 || tick == 2 || tick == TOTAL_TICKS) {
                System.out.println("Tick " + tick + ": " + requestsThisTick + " requests, "
                        + failedThisTick + " failed (system-wide error rate "
                        + String.format("%.0f", 100.0 * failedThisTick / requestsThisTick) + "%)");
            }
        }

        System.out.println();
        System.out.println("Experiment duration: " + TOTAL_TICKS + " ticks (ran to planned completion, no abort mechanism exists)");
        System.out.println("Total requests affected: " + totalRequests + ", total failed: " + totalFailed
                + " (" + String.format("%.0f", 100.0 * totalFailed / totalRequests) + "% blast radius)");
    }
}
