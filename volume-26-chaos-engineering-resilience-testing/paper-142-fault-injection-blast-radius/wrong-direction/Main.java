/**
 * WRONG DIRECTION: after the pressure scenario shows a 100% blast radius, the team scopes the
 * fault injection to a 10% "canary" slice of traffic instead of the whole system -- a real,
 * meaningful improvement on its own. But this version still has no abort condition: once the
 * canary segment's error rate is confirmed to have spiked, nothing stops the experiment early.
 * It runs for its full planned duration anyway, needlessly sustaining harm to that 10% slice
 * for the entire window instead of stopping the moment the result is already known.
 */
public class Main {
    private static final int TOTAL_TICKS = 30;
    private static final int REQUESTS_PER_TICK = 100;
    private static final double CANARY_FRACTION = 0.10;

    public static void main(String[] args) {
        long totalRequests = 0;
        long totalCanaryRequests = 0;
        long totalFailed = 0;

        int canaryPerTick = (int) Math.round(REQUESTS_PER_TICK * CANARY_FRACTION);

        for (int tick = 1; tick <= TOTAL_TICKS; tick++) {
            int requestsThisTick = REQUESTS_PER_TICK;
            // Only the canary-scoped slice is routed through the faulted inventory-service call;
            // the rest of production traffic is unaffected.
            int canaryThisTick = canaryPerTick;
            int failedThisTick = canaryThisTick;

            totalRequests += requestsThisTick;
            totalCanaryRequests += canaryThisTick;
            totalFailed += failedThisTick;

            if (tick == 1 || tick == 2 || tick == TOTAL_TICKS) {
                System.out.println("Tick " + tick + ": " + canaryThisTick + " of " + requestsThisTick
                        + " requests canary-scoped, all " + failedThisTick + " failed (canary error rate 100%, "
                        + "no abort check exists -- experiment continues)");
            }
        }

        System.out.println();
        System.out.println("Experiment duration: " + TOTAL_TICKS + " ticks (ran to planned completion despite the canary's failure being confirmed by tick 1)");
        System.out.println("Total requests affected: " + totalCanaryRequests + " of " + totalRequests
                + " system-wide, total failed: " + totalFailed + " (blast radius fixed to the canary slice, duration unchanged)");
    }
}
