import java.util.Random;

/**
 * Demonstrates the write cost of maintaining a B-Tree index under
 * random-order inserts: every insert can touch multiple nodes (its own
 * leaf, plus every ancestor node that has to split to make room), so
 * the total number of node writes grows faster than the number of keys
 * inserted -- this is write amplification.
 */
public class Main {
    public static void main(String[] args) {
        int totalKeys = 10_000;
        BTree tree = new BTree(32); // minimum degree 32, a realistic on-disk B-Tree fanout

        Random random = new Random(42);
        int[] keys = new int[totalKeys];
        for (int i = 0; i < totalKeys; i++) {
            keys[i] = i;
        }
        // Shuffle into random insertion order -- the common case for a
        // primary key that isn't monotonically increasing (e.g. a UUID
        // or a hash-based key), which is exactly what stresses a B-Tree.
        for (int i = totalKeys - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int tmp = keys[i];
            keys[i] = keys[j];
            keys[j] = tmp;
        }

        for (int key : keys) {
            tree.insert(key);
        }

        System.out.println("Keys inserted: " + totalKeys);
        System.out.println("Total node writes: " + tree.nodeWrites());
        double writeAmplification = (double) tree.nodeWrites() / totalKeys;
        System.out.printf("Write amplification (node writes per key): %.2fx%n", writeAmplification);

        // Lookup cost for a present key and a missing key -- both should
        // be small and roughly logarithmic in the number of keys, which
        // is the B-Tree's actual strength.
        int presentKey = keys[0];
        int missingKey = totalKeys + 500;
        System.out.println("Node reads for a present-key lookup: " + tree.lookupCost(presentKey));
        System.out.println("Node reads for a missing-key lookup: " + tree.lookupCost(missingKey));
    }
}
