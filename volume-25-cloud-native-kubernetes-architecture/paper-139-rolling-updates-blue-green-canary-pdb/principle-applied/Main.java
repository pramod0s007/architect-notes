/**
 * PRINCIPLE APPLIED: the PDB is sized to the actual availability floor the service needs
 * (minAvailable=7 of 10 -- the same 7 the load-serving requirement uses), not to "100% just
 * to be safe." This does two things correctly at once. First, it still doesn't gate the
 * rolling update's own pod replacement (same real-world limitation as the wrong-direction
 * scenario -- Deployments don't go through the Eviction API), so the deploy proceeds exactly
 * as it would with no PDB at all. Second, and this is the actual fix: because the PDB's
 * eviction check is evaluated against *current* available replicas (whatever the deploy has
 * already done), it naturally adapts to already-reduced capacity from any cause -- when the
 * drain requests 3 evictions while the deploy has already taken the fleet to 8, the PDB
 * allows only 1 (enough to stay at the 7 floor) instead of all 3, and lets the drain finish
 * evicting the rest as capacity recovers. This substantially reduces the stacking risk and
 * lets the drain complete (unlike the wrong-direction scenario, where it never can) -- but
 * it is not a perfect guarantee: the PDB only checks availability at the moment of *its own*
 * eviction request, so it has no way to know the deploy is about to take its next wave down
 * one tick later. With this deploy's back-to-back wave cadence (no idle tick between a wave
 * finishing and the next one starting), that timing coincidence can still produce one small,
 * transient dip below the floor -- far smaller and shorter than the unmanaged pressure
 * scenario, but not literally zero.
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
    private static final int DRAIN_REPLACEMENT_LAG_TICKS = 2;
    private static final int PDB_MIN_AVAILABLE = 7; // matches the real availability floor
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
        int evictedThisTick;
        java.util.List<Integer> pendingDrainBringUps = new java.util.ArrayList<>();

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

            evictedThisTick = 0;
            if (podsStillToEvict > 0) {
                int disruptionsAllowed = Math.max(0, fleet.available() - PDB_MIN_AVAILABLE);
                int evictNow = Math.min(disruptionsAllowed, podsStillToEvict);
                if (evictNow > 0) {
                    fleet.takeDown(evictNow);
                    podsStillToEvict -= evictNow;
                    evictedThisTick = evictNow;
                    pendingDrainBringUps.add(tick + DRAIN_REPLACEMENT_LAG_TICKS);
                    System.out.println("Tick " + tick + ": PDB allows evicting " + evictNow
                            + " replica(s) (minAvailable=" + PDB_MIN_AVAILABLE + " respected, available now "
                            + fleet.available() + "), " + podsStillToEvict + " still pending");
                } else {
                    deniedAttempts++;
                }
            }

            java.util.Iterator<Integer> it = pendingDrainBringUps.iterator();
            while (it.hasNext()) {
                if (it.next() == tick) {
                    fleet.bringUp(1);
                    it.remove();
                    System.out.println("Tick " + tick + ": a drained replica rescheduled and ready "
                            + "(available now " + fleet.available() + ")");
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
