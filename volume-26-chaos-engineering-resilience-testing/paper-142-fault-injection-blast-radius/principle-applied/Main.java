/**
 * PRINCIPLE APPLIED: same 10% canary scoping as the wrong-direction scenario, but this version
 * adds an automatic abort condition -- a predeclared safety abort threshold ("canary error rate
 * stays under 5%") that is checked every tick. The instant the canary segment's measured error
 * rate crosses that threshold, the experiment aborts immediately instead of running to its
 * planned duration. This bounds both the blast radius (still just the canary slice) AND the
 * experiment's duration (stops as soon as harm is confirmed, not after a fixed planned window).
 */
public class Main {
    private static final int TOTAL_TICKS = 30;
    private static final int REQUESTS_PER_TICK = 100;
    private static final double CANARY_FRACTION = 0.10;
    private static final double ABORT_ERROR_RATE_THRESHOLD = 0.05; // predeclared safety abort threshold: canary error rate stays under 5%

    public static void main(String[] args) {
        long totalRequests = 0;
        long totalCanaryRequests = 0;
        long totalFailed = 0;
        int actualTicksRun = 0;
        boolean aborted = false;

        int canaryPerTick = (int) Math.round(REQUESTS_PER_TICK * CANARY_FRACTION);

        for (int tick = 1; tick <= TOTAL_TICKS; tick++) {
            int requestsThisTick = REQUESTS_PER_TICK;
            int canaryThisTick = canaryPerTick;
            int failedThisTick = canaryThisTick;

            totalRequests += requestsThisTick;
            totalCanaryRequests += canaryThisTick;
            totalFailed += failedThisTick;
            actualTicksRun = tick;

            double canaryErrorRate = (double) failedThisTick / canaryThisTick;
            System.out.println("Tick " + tick + ": " + canaryThisTick + " of " + requestsThisTick
                    + " requests canary-scoped, " + failedThisTick + " failed (canary error rate "
                    + String.format("%.0f", canaryErrorRate * 100) + "%)");

            if (canaryErrorRate > ABORT_ERROR_RATE_THRESHOLD) {
                System.out.println("  -> predeclared safety abort threshold violated (canary error rate "
                        + String.format("%.0f", canaryErrorRate * 100) + "% > "
                        + String.format("%.0f", ABORT_ERROR_RATE_THRESHOLD * 100) + "% threshold) -- aborting experiment");
                aborted = true;
                break;
            }
        }

        System.out.println();
        System.out.println("Experiment duration: " + actualTicksRun + " of " + TOTAL_TICKS
                + " planned ticks (aborted: " + aborted + ")");
        System.out.println("Total requests affected: " + totalCanaryRequests + " of " + totalRequests
                + " system-wide, total failed: " + totalFailed + " -- both blast radius and duration bounded");
    }
}
