/**
 * PRESSURE: a Deployment-style workload with no per-replica identity or persistent storage --
 * each replica keeps a local cache/index that speeds up its own work, but on every reschedule
 * (node maintenance, eviction, etc.) the replacement pod gets a brand-new, empty ephemeral
 * volume with no relationship to whatever the previous pod had built up. This is the problem
 * StatefulSets exist to solve: without a stable identity to bind storage to, every reschedule
 * throws away all accumulated local state, regardless of how long it took to build.
 */
public class Main {
    private static final int REPLICAS = 3;
    private static final int TOTAL_TICKS = 40;
    private static final int RESCHEDULE_INTERVAL_TICKS = 10;

    public static void main(String[] args) {
        int[] lastResetTick = new int[REPLICAS];
        int resetEvents = 0;
        long cumulativeLostWorkTicks = 0;

        for (int tick = 1; tick <= TOTAL_TICKS; tick++) {
            if (tick % RESCHEDULE_INTERVAL_TICKS == 0) {
                for (int i = 0; i < REPLICAS; i++) {
                    cumulativeLostWorkTicks += (tick - lastResetTick[i]);
                    lastResetTick[i] = tick;
                    resetEvents++;
                }
                System.out.println("Tick " + tick + ": all " + REPLICAS
                        + " replicas rescheduled onto new nodes -- each gets a brand-new empty volume, "
                        + "local cache/index reset to nothing");
            }
        }

        System.out.println();
        System.out.println("Total replica-resets: " + resetEvents
                + ", cumulative ticks of built-up cache work lost: " + cumulativeLostWorkTicks);
    }
}
