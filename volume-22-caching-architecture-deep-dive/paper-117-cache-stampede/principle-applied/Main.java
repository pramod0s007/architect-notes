import java.util.Map;

/**
 * The real fix: instead of guessing a jitter window and hoping the spread
 * is even enough, compute the minimum window size the key population
 * actually requires to stay under the backend's real capacity, then
 * deterministically stagger every key's expiry evenly across that window
 * -- guaranteeing (not merely hoping) that no single tick ever receives
 * more calls than the backend can safely absorb.
 */
public class Main {
    private static final int KEY_COUNT = 1000;
    private static final int BASE_TTL = 100;
    private static final int BACKEND_CAPACITY_PER_TICK = 50;

    public static void main(String[] args) {
        CacheWarmer warmer = new CacheWarmer();
        BackendLoadTracker tracker = new BackendLoadTracker();

        // The minimum stagger window guaranteed to keep every tick at or under capacity.
        int requiredWindow = (int) Math.ceil((double) KEY_COUNT / BACKEND_CAPACITY_PER_TICK);

        Map<Integer, Integer> expiryByKey = warmer.warm(KEY_COUNT, BASE_TTL,
                (keyIndex, baseTtl) -> baseTtl + (keyIndex % requiredWindow));

        Map<Integer, Integer> callsPerTick = tracker.callsPerTick(expiryByKey);
        int peak = tracker.peakCallsAtAnyTick(callsPerTick);

        System.out.println("=== " + KEY_COUNT + " keys warmed at tick 0, deterministically staggered across a "
                + "computed window of " + requiredWindow + " ticks (ceil(" + KEY_COUNT + " / "
                + BACKEND_CAPACITY_PER_TICK + ")) ===");
        System.out.println("Distinct expiry ticks used: " + callsPerTick.size());
        System.out.println("Peak simultaneous backend calls at any single tick: " + peak);
        System.out.println("Backend capacity per tick: " + BACKEND_CAPACITY_PER_TICK);

        if (peak <= BACKEND_CAPACITY_PER_TICK) {
            System.out.println();
            System.out.println("FIXED: the stagger window was computed from the actual key count and the "
                    + "actual backend capacity, not guessed -- every one of the " + requiredWindow + " ticks "
                    + "in the window receives at most " + peak + " calls, at or under the backend's safe "
                    + "capacity of " + BACKEND_CAPACITY_PER_TICK + ", guaranteed by construction rather than "
                    + "by hoping a random spread happens to land evenly.");
        }
    }
}
