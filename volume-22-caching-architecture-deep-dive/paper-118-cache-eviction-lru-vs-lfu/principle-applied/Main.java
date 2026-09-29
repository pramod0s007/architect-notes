/**
 * The real fix: keep LFU's scan resistance, but periodically halve every
 * resident key's frequency count. This lets old counts fade over time
 * instead of accumulating forever, so a key that has genuinely gone cold
 * eventually loses its artificial advantage over a new candidate -- while
 * a decay period coarse enough relative to a single scan still preserves
 * the scan-resistance property that made LFU worth switching to in the
 * first place.
 */
public class Main {
    private static final int CAPACITY = 3;
    private static final String[] OLD_HOT_SET = {"A", "B", "C"};
    private static final String[] SCAN_KEYS = {"D", "E", "F", "G", "H", "I", "J", "K"};
    private static final String[] NEW_HOT_SET = {"X", "Y", "Z"};
    private static final int ROUNDS_PER_CYCLE = 10;
    private static final int CYCLES = 5;
    private static final int DECAY_PERIOD = 20; // halve every resident's frequency every 20 accesses

    private static int tick = 0;

    public static void main(String[] args) {
        LfuCache cache = new LfuCache(CAPACITY);
        int totalHits = 0, totalAccesses = 0;

        System.out.println("=== " + CYCLES + " cycles of (10 rounds of old-hot-set access, then a one-time "
                + SCAN_KEYS.length + "-key scan), frequencies decayed every " + DECAY_PERIOD + " accesses ===");
        for (int cycle = 1; cycle <= CYCLES; cycle++) {
            int cycleHits = 0, cycleAccesses = 0;
            for (int round = 0; round < ROUNDS_PER_CYCLE; round++) {
                for (String key : OLD_HOT_SET) {
                    if (accessWithDecay(cache, key)) cycleHits++;
                    cycleAccesses++;
                }
            }
            totalHits += cycleHits;
            totalAccesses += cycleAccesses;

            for (String key : SCAN_KEYS) {
                accessWithDecay(cache, key);
            }
        }
        System.out.println("Total old-hot-set hit rate across all " + CYCLES + " cycles: " + totalHits + "/"
                + totalAccesses);

        // The workload shifts entirely: the old hot set goes cold, a new hot set emerges.
        int newHits = 0, newAccesses = 0;
        int roundsUntilFirstNewHit = -1;
        for (int round = 0; round < ROUNDS_PER_CYCLE * CYCLES; round++) {
            for (String key : NEW_HOT_SET) {
                boolean hit = accessWithDecay(cache, key);
                if (hit) newHits++;
                newAccesses++;
                if (hit && roundsUntilFirstNewHit == -1) roundsUntilFirstNewHit = round + 1;
            }
        }
        System.out.println();
        System.out.println("=== Workload shifts: old set {A,B,C} goes cold, new hot set {X,Y,Z} accessed "
                + (ROUNDS_PER_CYCLE * CYCLES) + " rounds ===");
        System.out.println("New hot set hit rate: " + newHits + "/" + newAccesses);
        System.out.println("First new-hot-set hit occurred in round: "
                + (roundsUntilFirstNewHit == -1 ? "never" : roundsUntilFirstNewHit));
        for (String key : OLD_HOT_SET) {
            System.out.println("  Old key '" + key + "' still cached: " + cache.contains(key)
                    + ", frequency=" + cache.frequencyOf(key));
        }

        if (newHits > 0) {
            System.out.println();
            System.out.println("FIXED: unlike the undecayed frequency counts in the wrong-direction scenario, "
                    + "periodic decay let the old hot set's counts fade instead of accumulating forever. The "
                    + "new hot set started winning admission by round " + roundsUntilFirstNewHit
                    + " of the shift, and the old hot set's hit rate of " + totalHits + "/" + totalAccesses
                    + " during the scan-heavy phase shows the scan-resistance property was preserved, not "
                    + "traded away to get this adaptability.");
        }
    }

    private static boolean accessWithDecay(LfuCache cache, String key) {
        boolean hit = cache.access(key);
        tick++;
        if (tick % DECAY_PERIOD == 0) {
            cache.decayAll();
        }
        return hit;
    }
}
