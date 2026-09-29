/**
 * The team's fix after the scan-pollution incident: switch from LRU to a
 * pure frequency-counting cache. This genuinely fixes the scan-pollution
 * failure -- a one-time scan can never evict a key with a real access
 * history. But frequency counts that never decay create a different
 * failure: once a workload shifts and a new set of keys becomes the real
 * hot set, their fresh frequency counts can never catch up to the old
 * hot set's permanently high counts, so the cache never adapts, even
 * long after the old keys have stopped being accessed at all.
 */
public class Main {
    private static final int CAPACITY = 3;
    private static final String[] OLD_HOT_SET = {"A", "B", "C"};
    private static final String[] SCAN_KEYS = {"D", "E", "F", "G", "H", "I", "J", "K"};
    private static final String[] NEW_HOT_SET = {"X", "Y", "Z"};
    private static final int ROUNDS_PER_CYCLE = 10;
    private static final int CYCLES = 5;

    public static void main(String[] args) {
        LfuCache cache = new LfuCache(CAPACITY);
        int totalHits = 0, totalAccesses = 0;

        System.out.println("=== " + CYCLES + " cycles of (10 rounds of old-hot-set access, then a one-time "
                + SCAN_KEYS.length + "-key scan) -- same recurring-scan setup as the pressure scenario ===");
        for (int cycle = 1; cycle <= CYCLES; cycle++) {
            int cycleHits = 0, cycleAccesses = 0;
            for (int round = 0; round < ROUNDS_PER_CYCLE; round++) {
                for (String key : OLD_HOT_SET) {
                    if (cache.access(key)) cycleHits++;
                    cycleAccesses++;
                }
            }
            totalHits += cycleHits;
            totalAccesses += cycleAccesses;

            for (String key : SCAN_KEYS) {
                cache.access(key);
            }
        }
        System.out.println("Total old-hot-set hit rate across all " + CYCLES + " cycles: " + totalHits + "/"
                + totalAccesses);
        System.out.println("(Scan resistance genuinely works -- LFU fixed the pressure scenario's failure)");

        // The workload shifts entirely: the old hot set goes cold, a new hot set emerges.
        int newHits = 0, newAccesses = 0;
        for (int round = 0; round < ROUNDS_PER_CYCLE * CYCLES; round++) {
            for (String key : NEW_HOT_SET) {
                if (cache.access(key)) newHits++;
                newAccesses++;
            }
        }
        System.out.println();
        System.out.println("=== Workload shifts: old set {A,B,C} goes cold, new hot set {X,Y,Z} accessed "
                + (ROUNDS_PER_CYCLE * CYCLES) + " rounds ===");
        System.out.println("New hot set hit rate: " + newHits + "/" + newAccesses);
        for (String key : OLD_HOT_SET) {
            System.out.println("  Old key '" + key + "' still occupying cache, frequency=" + cache.frequencyOf(key));
        }

        if (newHits == 0) {
            System.out.println();
            System.out.println("CONFIRMED WRONG DIRECTION: LFU fixed scan pollution -- " + totalHits + "/"
                    + totalAccesses + " hit rate held steady across " + CYCLES + " recurring scans -- but its "
                    + "frequency counts never decay. The old hot set's counts from warm-up remain permanently "
                    + "higher than any new candidate's starting count of 1, so the cache stays pinned to keys "
                    + "that haven't been accessed in " + newAccesses + " accesses, while the actual current "
                    + "working set gets a 0/" + newAccesses + " hit rate for as long as the workload keeps "
                    + "shifting.");
        }
    }
}
