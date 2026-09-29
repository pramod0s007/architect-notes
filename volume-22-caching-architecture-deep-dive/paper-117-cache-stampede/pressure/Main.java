import java.util.Map;

/**
 * Simulates a bulk cache warm-up: 1000 keys populated at the same instant,
 * all with the identical fixed TTL. Every one of them expires at exactly
 * the same tick, so every one of them triggers a backend recompute call
 * at exactly the same instant -- a stampede across the whole key
 * population, not a race between multiple requests for one key (that
 * narrower case, and its single-flight fix, is a different problem
 * covered in an earlier paper in this series).
 */
public class Main {
    private static final int KEY_COUNT = 1000;
    private static final int BASE_TTL = 100;
    private static final int BACKEND_CAPACITY_PER_TICK = 50;

    public static void main(String[] args) {
        CacheWarmer warmer = new CacheWarmer();
        BackendLoadTracker tracker = new BackendLoadTracker();

        Map<Integer, Integer> expiryByKey = warmer.warm(KEY_COUNT, BASE_TTL,
                (keyIndex, baseTtl) -> baseTtl); // every key gets the SAME fixed expiry tick

        Map<Integer, Integer> callsPerTick = tracker.callsPerTick(expiryByKey);
        int peak = tracker.peakCallsAtAnyTick(callsPerTick);

        System.out.println("=== " + KEY_COUNT + " keys warmed at tick 0, all with a fixed TTL of "
                + BASE_TTL + " ticks ===");
        System.out.println("Distinct expiry ticks used: " + callsPerTick.size());
        System.out.println("Peak simultaneous backend calls at any single tick: " + peak);
        System.out.println("Backend capacity per tick: " + BACKEND_CAPACITY_PER_TICK);

        if (peak > BACKEND_CAPACITY_PER_TICK) {
            System.out.println();
            System.out.println("CONFIRMED FAILURE: all " + KEY_COUNT + " keys share the exact same expiry "
                    + "tick, because they were all warmed at the same instant with an identical fixed TTL. "
                    + "The backend receives " + peak + " simultaneous recompute calls at that single tick -- "
                    + peak / BACKEND_CAPACITY_PER_TICK + "x its safe capacity of " + BACKEND_CAPACITY_PER_TICK
                    + " -- even though no two requests are for the same key, so a per-key single-flight lock "
                    + "would provide zero protection here.");
        }
    }
}
