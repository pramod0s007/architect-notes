/**
 * WRONG DIRECTION: the operator sees PodA's frequent OOMKilled restarts on a dashboard and
 * treats it as a mis-sized limit -- the obvious, well-intentioned fix is to remove the
 * memory limit entirely (limitMB = 0, uncapped) so it stops "crashing." The restarts do
 * stop. But PodA's actual problem was never a too-tight limit -- it's a real leak -- and
 * removing its ceiling doesn't fix the leak, it just removes the only thing that was
 * containing it. Usage now grows every tick with nothing to stop it, and once PodA's
 * growth plus PodB's steady, unrelated usage exceeds the node's total memory, the node
 * itself enters memory pressure -- a real, well-documented kubelet condition where the
 * kubelet reclaims memory by evicting pods, not necessarily the one that caused the
 * pressure (real eviction ranks candidates by factors like usage vs. requests, so in many
 * real clusters the leaking pod itself would still be evicted first). Simulation: to
 * illustrate the expanded blast radius, this model treats PodB as the one that degrades
 * once node pressure hits, as a simplified worst-case stand-in for "some workload other
 * than the one that caused the problem may be the one that pays for it."
 */
public class Main {
    private static final int BASELINE_MB = 200;
    private static final int LEAK_RATE_MB_PER_TICK = 15;
    private static final int POD_A_LIMIT_MB = 0; // no limit configured
    private static final int POD_B_USAGE_MB = 300;
    private static final int NODE_CAPACITY_MB = 1000;
    private static final int TOTAL_TICKS = 60;

    public static void main(String[] args) {
        Pod podA = new Pod(BASELINE_MB, LEAK_RATE_MB_PER_TICK, POD_A_LIMIT_MB);
        boolean nodeUnderPressure = false;
        int podBDegradedAtTick = -1;
        int ticksPodBDegraded = 0;

        for (int tick = 1; tick <= TOTAL_TICKS; tick++) {
            podA.tick();
            int nodeUsedMB = podA.usageMB() + POD_B_USAGE_MB;

            if (!nodeUnderPressure && nodeUsedMB > NODE_CAPACITY_MB) {
                nodeUnderPressure = true;
                podBDegradedAtTick = tick;
                System.out.println("Tick " + tick + ": NODE MEMORY PRESSURE -- total usage " + nodeUsedMB
                        + "MB exceeds node capacity " + NODE_CAPACITY_MB
                        + "MB -- (simulation) PodB is modeled as degraded to illustrate the expanded blast radius");
            }

            if (nodeUnderPressure) {
                ticksPodBDegraded++;
            }
        }

        System.out.println();
        System.out.println("PodA total restarts: " + podA.restartCount() + ", PodA final usage: "
                + podA.usageMB() + "MB, node entered memory pressure at tick: " + podBDegradedAtTick
                + ", ticks PodB degraded as a result: " + ticksPodBDegraded);
    }
}
