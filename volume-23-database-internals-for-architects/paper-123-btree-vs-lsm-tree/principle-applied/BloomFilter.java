import java.util.BitSet;

/**
 * A simple Bloom filter: a fixed-size bit array with a small, fixed
 * number of independent hash functions per element. Adding a key sets
 * the bits at each hash function's position; checking membership tests
 * whether all of those bits are set. False positives are possible ("the
 * key might be in this SSTable") but false negatives are not ("the key
 * is definitely not in this SSTable" is always correct).
 */
public class BloomFilter {
    private final BitSet bits;
    private final int size;
    private final int hashCount;

    public BloomFilter(int expectedElements, int bitsPerElement, int hashCount) {
        this.size = Math.max(1, expectedElements * bitsPerElement);
        this.bits = new BitSet(size);
        this.hashCount = hashCount;
    }

    public void add(int key) {
        for (int i = 0; i < hashCount; i++) {
            bits.set(hash(key, i));
        }
    }

    /** True means "maybe present" (could be a false positive); false means "definitely absent." */
    public boolean mightContain(int key) {
        for (int i = 0; i < hashCount; i++) {
            if (!bits.get(hash(key, i))) {
                return false;
            }
        }
        return true;
    }

    private int hash(int key, int seedIndex) {
        // Two independent-enough hash functions combined (double hashing),
        // a standard, deterministic way to derive hashCount bit positions
        // from a single key without hashCount separate hash functions.
        int h1 = Integer.hashCode(key);
        int h2 = Integer.hashCode(key * 0x9E3779B1) ^ (key >>> 15);
        int combined = h1 + seedIndex * h2;
        return Math.floorMod(combined, size);
    }
}
