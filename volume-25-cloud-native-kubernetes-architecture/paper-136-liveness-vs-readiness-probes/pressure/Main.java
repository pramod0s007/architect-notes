/**
 * PRESSURE: only a liveness probe is configured, with a failure threshold (3 consecutive
 * failed probes) tighter than the pod's genuine warmup time (5 ticks to finish loading a
 * cache on startup). The probe can't tell "still legitimately warming up" from "actually
 * dead" -- both look like "not responding." The pod gets restarted before it ever
 * finishes warming up, resetting its warmup clock, and the cycle repeats forever.
 */
public class Main {
    private static final int WARMUP_TICKS = 5;
    private static final int LIVENESS_FAILURE_THRESHOLD = 3;

    public static void main(String[] args) {
        Pod pod = new Pod(WARMUP_TICKS);
        int failStreak = 0;
        int restarts = 0;

        for (int tick = 1; tick <= 20; tick++) {
            pod.tick();
            boolean livenessOk = pod.isWarmedUp();

            if (livenessOk) {
                failStreak = 0;
                System.out.println("Tick " + tick + ": liveness probe OK (pod warmed up)");
            } else {
                failStreak++;
                System.out.println("Tick " + tick + ": liveness probe FAILED (still warming up, streak " + failStreak + ")");
                if (failStreak >= LIVENESS_FAILURE_THRESHOLD) {
                    restarts++;
                    pod.restart(WARMUP_TICKS);
                    failStreak = 0;
                    System.out.println("  -> kubelet restarted the pod (restart #" + restarts + ") -- warmup resets to " + WARMUP_TICKS + " ticks");
                }
            }
        }

        System.out.println();
        System.out.println("Pod ever became ready: " + pod.isWarmedUp() + ", total restarts: " + restarts);
    }
}
