/**
 * PRINCIPLE APPLIED: two things change, not one. First, PodA's limit is sized from
 * realistic peak headroom (300MB, with real margin above baseline, not the 250MB limit
 * that was undersized against actual peak usage), so it isn't
 * hair-trigger about every small bump -- it still gets OOMKilled and restarted when the
 * leak actually reaches that ceiling, because containment is still the right behavior for
 * an unbounded leak. Second, repeated OOMKills are treated as a signal, not noise: once
 * PodA has been OOMKilled 3 times, an alert fires flagging it for investigation, instead of
 * either (a) letting it restart forever unnoticed, or (b) "fixing" the symptom by loosening
 * the ceiling. The leak is still there -- this doesn't patch the bug -- but it's surfaced
 * for a human to actually act on, and it never gets a chance to grow past PodA's own
 * boundary. PodB and the node stay completely healthy the entire time either way.
 */
public class Main {
    private static final int BASELINE_MB = 200;
    private static final int LEAK_RATE_MB_PER_TICK = 15;
    private static final int POD_A_LIMIT_MB = 300;
    private static final int POD_B_USAGE_MB = 300;
    private static final int NODE_CAPACITY_MB = 1000;
    private static final int TOTAL_TICKS = 60;
    private static final int RESTART_ALERT_THRESHOLD = 3;

    public static void main(String[] args) {
        Pod podA = new Pod(BASELINE_MB, LEAK_RATE_MB_PER_TICK, POD_A_LIMIT_MB);
        boolean nodeEverUnderPressure = false;
        boolean alertFired = false;

        for (int tick = 1; tick <= TOTAL_TICKS; tick++) {
            boolean oomKilled = podA.tick();
            int nodeUsedMB = podA.usageMB() + POD_B_USAGE_MB;

            if (oomKilled) {
                System.out.println("Tick " + tick + ": PodA OOMKilled (exceeded " + POD_A_LIMIT_MB
                        + "MB limit) -- restarted, usage reset to " + BASELINE_MB + "MB (restart #"
                        + podA.restartCount() + ")");

                if (!alertFired && podA.restartCount() >= RESTART_ALERT_THRESHOLD) {
                    alertFired = true;
                    System.out.println("  -> ALERT: PodA has OOMKilled " + podA.restartCount()
                            + " times -- likely memory leak, flagged for investigation "
                            + "(limit stays in place; this does not raise the ceiling)");
                }
            }

            if (nodeUsedMB > NODE_CAPACITY_MB) {
                nodeEverUnderPressure = true;
            }
        }

        System.out.println();
        System.out.println("PodA total restarts: " + podA.restartCount() + ", alert fired: " + alertFired
                + ", node ever under memory pressure: " + nodeEverUnderPressure);
    }
}
