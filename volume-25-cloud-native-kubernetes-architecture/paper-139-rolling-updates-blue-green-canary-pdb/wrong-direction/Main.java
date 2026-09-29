/**
 * WRONG DIRECTION: after seeing dropped requests from the pressure scenario, the operator
 * concludes "we need a PodDisruptionBudget to protect availability" -- true -- and sets
 * minAvailable to 100% of replicas (10 of 10), reasoning that maximum availability should
 * mean maximum protection. But a PDB only governs voluntary disruptions performed through
 * the Eviction API (node drains, cluster-autoscaler scale-down, `kubectl drain`) -- it does
 * NOT govern a Deployment controller's own pod replacement during a rolling update, which
 * uses direct pod deletion, not the Eviction API. So this "fix" has zero effect on the
 * scenario it was meant to protect: the deploy still dips to 8 of 10 on its own (never below
 * the 7-replica floor, so 0 dropped requests either way). What it actually does is set
 * disruptionsAllowed to 0 for the rest of this run -- since minAvailable equals the fleet's
 * fixed total replica count, no tick in this simulation ever has spare replicas above that
 * ceiling, so the unrelated node drain can never evict a single pod through the Eviction API
 * (in a real cluster, temporary headroom above minAvailable -- e.g. from surge capacity --
 * could still open a brief eviction window; this fixed-size fleet has no such headroom).
 * The maintenance operation stays blocked for as long as that headroom never materializes,
 * for a problem this PDB was never actually protecting against.
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
    private static final int PDB_MIN_AVAILABLE = 10; // 100% -- no slack for any eviction, ever
    private static final int TOTAL_TICKS = 30;

    public static void main(String[] args) {
        Fleet fleet = new Fleet(TOTAL_REPLICAS);
        int wave = 0;
        int ticksSinceWaveStart = -1;
        boolean deployDone = false;
        long droppedRequests = 0;
        int minAvailableSeen = TOTAL_REPLICAS;
        int podsStillToEvict = 0;
        int deniedAttempts = 0;

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

            if (tick == DRAIN_TICK) {
                podsStillToEvict = DRAIN_EVICT_REQUEST;
            }

            if (podsStillToEvict > 0) {
                int disruptionsAllowed = Math.max(0, fleet.available() - PDB_MIN_AVAILABLE);
                int evictNow = Math.min(disruptionsAllowed, podsStillToEvict);
                if (evictNow > 0) {
                    fleet.takeDown(evictNow);
                    podsStillToEvict -= evictNow;
                } else {
                    deniedAttempts++;
                }
            }

            minAvailableSeen = Math.min(minAvailableSeen, fleet.available());
            if (fleet.available() < REQUIRED_FOR_FULL_CAPACITY) {
                droppedRequests += (long) (REQUIRED_FOR_FULL_CAPACITY - fleet.available())
                        * DROPPED_PER_TICK_PER_MISSING_REPLICA;
            }
        }

        System.out.println();
        System.out.println("Minimum available replicas seen: " + minAvailableSeen
                + ", total dropped requests: " + droppedRequests + ", drain pods evicted: "
                + (DRAIN_EVICT_REQUEST - podsStillToEvict) + " of " + DRAIN_EVICT_REQUEST
                + ", denied eviction attempts: " + deniedAttempts);
    }
}
