/**
 * PRESSURE: PodA has a real, undetected memory leak (usage grows every tick and never
 * stops on its own). Its memory limit (250MB) was sized from steady-state observation, not
 * from any actual peak, so it caps the leak fairly quickly on every cycle -- PodA gets
 * OOMKilled and restarted repeatedly. This looks bad in isolation (frequent restarts), but
 * the limit is doing exactly its job: the failure never grows past PodA's own boundary.
 * PodB (a normal, unrelated, non-leaking service on the same node) and the node itself
 * stay completely healthy the entire time. The problem is that nobody is looking at *why*
 * PodA keeps restarting -- a real leak is being silently masked by containment, not fixed.
 */
public class Main {
    private static final int BASELINE_MB = 200;
    private static final int LEAK_RATE_MB_PER_TICK = 15;
    private static final int POD_A_LIMIT_MB = 250;
    private static final int POD_B_USAGE_MB = 300;
    private static final int NODE_CAPACITY_MB = 1000;
    private static final int TOTAL_TICKS = 60;

    public static void main(String[] args) {
        Pod podA = new Pod(BASELINE_MB, LEAK_RATE_MB_PER_TICK, POD_A_LIMIT_MB);
        boolean nodeEverUnderPressure = false;

        for (int tick = 1; tick <= TOTAL_TICKS; tick++) {
            boolean oomKilled = podA.tick();
            int nodeUsedMB = podA.usageMB() + POD_B_USAGE_MB;

            if (oomKilled) {
                System.out.println("Tick " + tick + ": PodA OOMKilled (exceeded " + POD_A_LIMIT_MB
                        + "MB limit) -- restarted, usage reset to " + BASELINE_MB + "MB (restart #"
                        + podA.restartCount() + ")");
            }

            if (nodeUsedMB > NODE_CAPACITY_MB) {
                nodeEverUnderPressure = true;
                System.out.println("Tick " + tick + ": NODE MEMORY PRESSURE -- total usage " + nodeUsedMB
                        + "MB exceeds node capacity " + NODE_CAPACITY_MB + "MB");
            }
        }

        System.out.println();
        System.out.println("PodA total restarts: " + podA.restartCount() + ", PodA final usage: "
                + podA.usageMB() + "MB, node ever under memory pressure: " + nodeEverUnderPressure);
    }
}
