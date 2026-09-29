import java.util.Map;
import java.util.Random;

/**
 * The team's fix after the stampede incident: add a small random jitter to
 * each key's TTL, so not every key expires at the exact same tick. This is
 * a real, measurable improvement -- but the jitter window is much
 * narrower than the key population needs, so the resulting spread still
 * concentrates far more load onto individual ticks than the backend can
 * safely absorb.
 */
public class Main {
    private static final int KEY_COUNT = 1000;
    private static final int BASE_TTL = 100;
    private static final int JITTER_RANGE = 5; // expiry tick = BASE_TTL +/- JITTER_RANGE
    private static final int BACKEND_CAPACITY_PER_TICK = 50;
    private static final long FIXED_SEED = 42L; // deterministic, reproducible jitter

    public static void main(String[] args) {
        CacheWarmer warmer = new CacheWarmer();
        BackendLoadTracker tracker = new BackendLoadTracker();
        Random random = new Random(FIXED_SEED);

        Map<Integer, Integer> expiryByKey = warmer.warm(KEY_COUNT, BASE_TTL,
                (keyIndex, baseTtl) -> baseTtl + random.nextInt(2 * JITTER_RANGE + 1) - JITTER_RANGE);

        Map<Integer, Integer> callsPerTick = tracker.callsPerTick(expiryByKey);
        int peak = tracker.peakCallsAtAnyTick(callsPerTick);

        System.out.println("=== " + KEY_COUNT + " keys warmed at tick 0, TTL jittered +/- " + JITTER_RANGE
                + " ticks around a base of " + BASE_TTL + " ===");
        System.out.println("Distinct expiry ticks used: " + callsPerTick.size()
                + " (spread across a window of " + (2 * JITTER_RANGE + 1) + " ticks)");
        System.out.println("Peak simultaneous backend calls at any single tick: " + peak);
        System.out.println("Backend capacity per tick: " + BACKEND_CAPACITY_PER_TICK);
        System.out.println("(Down from 1000 in the pressure scenario -- looks like the fix worked)");

        if (peak > BACKEND_CAPACITY_PER_TICK) {
            System.out.println();
            System.out.println("CONFIRMED WRONG DIRECTION: jitter genuinely spread the 1000 keys across "
                    + (2 * JITTER_RANGE + 1) + " distinct ticks instead of 1, and the peak dropped sharply. "
                    + "But the jitter window was sized without reference to how many keys actually needed to "
                    + "fit inside it -- spreading 1000 keys across only " + (2 * JITTER_RANGE + 1)
                    + " ticks still averages roughly " + (KEY_COUNT / (2 * JITTER_RANGE + 1))
                    + " calls per tick, and random spread is uneven enough that the actual busiest tick still "
                    + "received " + peak + " calls, still over the backend's safe capacity of "
                    + BACKEND_CAPACITY_PER_TICK + ".");
        }
    }
}
