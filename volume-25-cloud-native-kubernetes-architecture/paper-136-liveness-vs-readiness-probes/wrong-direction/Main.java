/**
 * WRONG DIRECTION: "fix" the crash loop by raising the liveness failure threshold (10)
 * well past the pod's warmup time (5 ticks), so the probe no longer restarts the pod
 * during legitimate warmup. This works -- zero restarts. But there is still no
 * readiness probe configured anywhere in the system, and a running container with no
 * readiness probe is treated by Kubernetes as ready for Service traffic by default --
 * true from tick 1, long before it's actually finished loading its cache. Liveness
 * never gated traffic in the first place, so raising its threshold couldn't have fixed
 * that. Every request sent during the real warmup window fails.
 */
public class Main {
    private static final int WARMUP_TICKS = 5;
    private static final int LIVENESS_FAILURE_THRESHOLD = 10;

    public static void main(String[] args) {
        Pod pod = new Pod(WARMUP_TICKS);
        int failStreak = 0;
        int restarts = 0;
        int requestsSent = 0;
        int requestsFailed = 0;

        for (int tick = 1; tick <= 20; tick++) {
            pod.tick();
            boolean livenessOk = pod.isWarmedUp();

            if (livenessOk) {
                failStreak = 0;
            } else {
                failStreak++;
                if (failStreak >= LIVENESS_FAILURE_THRESHOLD) {
                    restarts++;
                    pod.restart(WARMUP_TICKS);
                    failStreak = 0;
                }
            }

            // No readiness probe configured -- Kubernetes' default behavior is to treat a
            // running container as ready for Service traffic, regardless of whether it's
            // actually finished warming up. Liveness never gated traffic to begin with.
            requestsSent++;
            boolean requestSucceeded = pod.isWarmedUp();
            if (!requestSucceeded) {
                requestsFailed++;
            }
            System.out.println("Tick " + tick + ": request routed to pod -- "
                    + (requestSucceeded ? "succeeded" : "FAILED (still warming up, but considered available)"));
        }

        System.out.println();
        System.out.println("Total requests sent: " + requestsSent + ", failed: " + requestsFailed + ", restarts: " + restarts);
    }
}
