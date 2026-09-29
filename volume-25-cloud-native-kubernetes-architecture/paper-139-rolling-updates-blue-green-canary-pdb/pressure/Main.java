/**
 * PRESSURE: a rolling update paces itself safely (maxUnavailable=2 of 10 replicas per wave,
 * waiting for replacements to become ready before the next wave), so in isolation it never
 * drops below the 7-replica floor needed to serve full load. But mid-rollout, a completely
 * unrelated node drain (routine maintenance, unaware of the deploy in progress) evicts 3
 * more replicas for its own reasons. With no PodDisruptionBudget configured, nothing checks
 * current availability before allowing that eviction -- the drain's "safe on its own" 3-pod
 * eviction stacks directly on top of the deploy's "safe on its own" 2-pod dip, and the fleet
 * drops to 5 of 10 available, well under the 7 needed, causing real dropped requests. Two
 * independently-reasonable operations, each configured correctly for itself, combined into a
 * capacity floor neither one was designed to allow. (The drain's evicted replicas do get
 * rescheduled and become ready again after their own lag, same as the deploy's own waves --
 * this isn't a permanent capacity loss, just an uncoordinated, stacked dip.)
 */
public class Main {
    private static final int TOTAL_REPLICAS = 10;
    private static final int REQUIRED_FOR_FULL_CAPACITY = 7;
    private static final int DROPPED_PER_TICK_PER_MISSING_REPLICA = 100;
    private static final int DEPLOY_MAX_UNAVAILABLE = 2;
    private static final int DEPLOY_WAVE_LAG_TICKS = 3;
    private static final int TOTAL_WAVES = TOTAL_REPLICAS / DEPLOY_MAX_UNAVAILABLE;
    private static final int DRAIN_TICK = 5;
    private static final int DRAIN_EVICT_REQUEST = 3;
    private static final int DRAIN_REPLACEMENT_LAG_TICKS = 4;
    private static final int TOTAL_TICKS = 30;

    public static void main(String[] args) {
        Fleet fleet = new Fleet(TOTAL_REPLICAS);
        int wave = 0;
        int ticksSinceWaveStart = -1;
        boolean deployDone = false;
        boolean drainRun = false;
        int drainBringUpAtTick = -1;
        long droppedRequests = 0;
        int minAvailableSeen = TOTAL_REPLICAS;

        for (int tick = 1; tick <= TOTAL_TICKS; tick++) {
            if (!deployDone) {
                if (ticksSinceWaveStart == -1) {
                    fleet.takeDown(DEPLOY_MAX_UNAVAILABLE);
                    ticksSinceWaveStart = 0;
                    System.out.println("Tick " + tick + ": rollout wave " + (wave + 1) + "/" + TOTAL_WAVES
                            + " takes down " + DEPLOY_MAX_UNAVAILABLE + " replicas (available now "
                            + fleet.available() + ")");
                } else {
                    ticksSinceWaveStart++;
                    if (ticksSinceWaveStart == DEPLOY_WAVE_LAG_TICKS) {
                        fleet.bringUp(DEPLOY_MAX_UNAVAILABLE);
                        wave++;
                        ticksSinceWaveStart = -1;
                        if (wave == TOTAL_WAVES) {
                            deployDone = true;
                        }
                        System.out.println("Tick " + tick + ": rollout wave " + wave + " replacements ready "
                                + "(available now " + fleet.available() + ")");
                    }
                }
            }

            if (tick == DRAIN_TICK && !drainRun) {
                drainRun = true;
                fleet.takeDown(DRAIN_EVICT_REQUEST);
                drainBringUpAtTick = tick + DRAIN_REPLACEMENT_LAG_TICKS;
                System.out.println("Tick " + tick + ": UNRELATED NODE DRAIN evicts " + DRAIN_EVICT_REQUEST
                        + " replicas for maintenance, no PDB to check current availability "
                        + "(available now " + fleet.available() + ")");
            }

            if (tick == drainBringUpAtTick) {
                fleet.bringUp(DRAIN_EVICT_REQUEST);
                System.out.println("Tick " + tick + ": drained replicas rescheduled and ready "
                        + "(available now " + fleet.available() + ")");
            }

            minAvailableSeen = Math.min(minAvailableSeen, fleet.available());
            if (fleet.available() < REQUIRED_FOR_FULL_CAPACITY) {
                droppedRequests += (long) (REQUIRED_FOR_FULL_CAPACITY - fleet.available())
                        * DROPPED_PER_TICK_PER_MISSING_REPLICA;
            }
        }

        System.out.println();
        System.out.println("Minimum available replicas seen: " + minAvailableSeen
                + ", total dropped requests: " + droppedRequests);
    }
}

