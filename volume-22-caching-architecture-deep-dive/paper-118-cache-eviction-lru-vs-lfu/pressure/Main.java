/**
 * Demonstrates LRU cache pollution: a small, genuinely hot working set
 * survives fine on its own, but a recurring bulk scan -- e.g. a periodic
 * backup job that touches many keys exactly once per run -- evicts the
 * hot set every single time it runs, purely because recency doesn't
 * distinguish "accessed once, never again" from "accessed constantly."
 * The scan's keys are never reused, yet each one still counts as more
 * "recent" than the hot set the moment it's touched, so the hot set has
 * to be re-fetched from the backend after every scan, forever.
 */
public class Main {
    private static final int CAPACITY = 3;
    private static final String[] HOT_SET = {"A", "B", "C"};
    private static final String[] SCAN_KEYS = {"D", "E", "F", "G", "H", "I", "J", "K"};
    private static final int ROUNDS_PER_CYCLE = 10;
    private static final int CYCLES = 5;

    public static void main(String[] args) {
        LruCache cache = new LruCache(CAPACITY);
        int totalHits = 0, totalAccesses = 0;

        System.out.println("=== " + CYCLES + " cycles of (10 rounds of hot-set access, then a one-time "
                + SCAN_KEYS.length + "-key scan) ===");
        for (int cycle = 1; cycle <= CYCLES; cycle++) {
            int cycleHits = 0, cycleAccesses = 0;
            for (int round = 0; round < ROUNDS_PER_CYCLE; round++) {
                for (String key : HOT_SET) {
                    if (cache.access(key)) cycleHits++;
                    cycleAccesses++;
                }
            }
            System.out.println("Cycle " + cycle + " hot-set hit rate: " + cycleHits + "/" + cycleAccesses);
            totalHits += cycleHits;
            totalAccesses += cycleAccesses;

            for (String key : SCAN_KEYS) {
                cache.access(key); // the recurring scan: 8 keys, each touched exactly once, never reused
            }
        }

        int totalMisses = totalAccesses - totalHits;
        System.out.println();
        System.out.println("Total hot-set hit rate across all " + CYCLES + " cycles: " + totalHits + "/"
                + totalAccesses);
        System.out.println("Total avoidable misses caused by the scan: " + totalMisses);

        if (totalMisses == CYCLES * HOT_SET.length) {
            System.out.println();
            System.out.println("CONFIRMED FAILURE: every single one of the " + CYCLES + " scans evicted the "
                    + "entire hot set, forcing exactly " + HOT_SET.length + " avoidable backend re-fetches per "
                    + "cycle -- " + totalMisses + " total -- for keys that were never actually cold. LRU "
                    + "eviction only looks at recency, so a one-time scan key always outranks a hot key the "
                    + "moment it's touched, no matter how many times that hot key has proven itself before.");
        }
    }
}
