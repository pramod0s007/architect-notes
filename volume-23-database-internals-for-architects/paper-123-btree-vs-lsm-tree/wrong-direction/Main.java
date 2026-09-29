import java.util.Random;

/**
 * Demonstrates the write-side win and the read-side failure of a plain
 * LSM-tree with no bloom filters: the same 10,000 random-order inserts
 * that cost a B-Tree over 10,000 scattered node writes cost this engine
 * only a handful of sequential flushes. But a lookup for a key that
 * isn't in any SSTable has to check every SSTable there is, because
 * nothing tells the engine which ones are worth skipping.
 */
public class Main {
    public static void main(String[] args) {
        int totalKeys = 10_000;
        LSMTree lsm = new LSMTree();

        Random random = new Random(42);
        int[] keys = new int[totalKeys];
        for (int i = 0; i < totalKeys; i++) {
            keys[i] = i;
        }
        for (int i = totalKeys - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int tmp = keys[i];
            keys[i] = keys[j];
            keys[j] = tmp;
        }

        for (int key : keys) {
            lsm.insert(key);
        }

        System.out.println("Keys inserted: " + totalKeys);
        System.out.println("Sequential flush operations: " + lsm.flushCount());
        System.out.println("SSTables on disk: " + lsm.sstableCount());

        // A key that was genuinely never inserted -- the worst case for
        // a bloom-filter-free LSM-tree, since every SSTable has to be
        // checked before concluding the key doesn't exist.
        int missingKey = totalKeys + 500;
        int lookupReads = lsm.lookupCost(missingKey);
        System.out.println("SSTable reads for a missing-key lookup: " + lookupReads
                + " (had to check all " + lsm.sstableCount() + " SSTables)");

        // A present key that only exists in the oldest SSTable -- also
        // expensive, because every newer SSTable gets checked first.
        int oldestKey = keys[0];
        int presentLookupReads = lsm.lookupCost(oldestKey);
        System.out.println("SSTable reads for a present-key lookup (oldest SSTable): " + presentLookupReads);
    }
}
