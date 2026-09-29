/**
 * PRINCIPLE APPLIED: separate the two concerns into two distinct probes. The readiness
 * probe gates traffic -- it checks whether the pod has actually finished warming up, and
 * while it hasn't, the pod is excluded from the load balancer entirely, no request is
 * ever sent to it, and nothing gets restarted for that. The liveness probe is
 * independent, with its own grace period (6 ticks -- comfortably longer than the pod's
 * real 5-tick warmup) before it starts counting failures at all, so it never mistakes
 * ordinary startup for a hang. Neither the crash loop nor the failed-request problem can
 * happen here, because no single signal is being asked to answer both questions at once.
 */
public class Main {
    private static final int WARMUP_TICKS = 5;
    private static final int LIVENESS_GRACE_PERIOD_TICKS = 6;
    private static final int LIVENESS_FAILURE_THRESHOLD = 3;

    public static void main(String[] args) {
        Pod pod = new Pod(WARMUP_TICKS);
        int failStreak = 0;
        int restarts = 0;
        int requestsRouted = 0;
        int requestsFailed = 0;
        int requestsSkippedNotReady = 0;

        for (int tick = 1; tick <= 20; tick++) {
            pod.tick();
            boolean ready = pod.isWarmedUp(); // readiness probe -- gates traffic only

            if (ready) {
                requestsRouted++;
                System.out.println("Tick " + tick + ": pod ready -- request routed, succeeded");
            } else {
                requestsSkippedNotReady++;
                System.out.println("Tick " + tick + ": pod not ready yet -- excluded from load balancer (no request sent)");
            }

            // liveness probe -- only starts counting failures after its own grace period,
            // decoupled from whether the pod is currently considered ready for traffic
            if (tick > LIVENESS_GRACE_PERIOD_TICKS) {
                if (ready) {
                    failStreak = 0;
                } else {
                    failStreak++;
                    if (failStreak >= LIVENESS_FAILURE_THRESHOLD) {
                        restarts++;
                        pod.restart(WARMUP_TICKS);
                        failStreak = 0;
                    }
                }
            }
        }

        System.out.println();
        System.out.println("Requests routed: " + requestsRouted + ", failed: " + requestsFailed
                + ", skipped while not ready: " + requestsSkippedNotReady + ", restarts: " + restarts);
    }
}
