import java.util.Random;

/**
 * Demonstrates that adding a Bloom filter per SSTable keeps the exact
 * same write-side win as the plain LSM-tree (same number of sequential
 * flushes for the same 10,000 inserts) while fixing the read-side
 * failure: a lookup for a missing key now performs far fewer actual
 * SSTable reads, because the Bloom filters correctly rule out SSTables
 * that don't contain the key without ever reading them.
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

        int missingKey = totalKeys + 500;
        int missingReads = lsm.lookupCost(missingKey);
        System.out.println("Actual SSTable reads for a missing-key lookup: " + missingReads
                + " (Bloom filters ruled out the other " + (lsm.sstableCount() - missingReads) + ")");

        int oldestKey = keys[0];
        int presentReads = lsm.lookupCost(oldestKey);
        System.out.println("Actual SSTable reads for a present-key lookup (oldest SSTable): " + presentReads);
    }
}
