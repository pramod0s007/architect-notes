import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * The same LSM-tree write path as the wrong-direction version (sequential
 * memtable appends, flushed as sorted SSTables), but every SSTable is
 * now paired with a Bloom filter built at flush time. A lookup checks
 * each SSTable's Bloom filter first, in memory, and only performs the
 * actual (comparatively expensive) SSTable read if the filter says the
 * key might be present -- turning "check every SSTable" into "check
 * only the SSTables that could possibly contain the key."
 */
public class LSMTree {
    private static final int MEMTABLE_FLUSH_THRESHOLD = 500;
    private static final int BLOOM_BITS_PER_ELEMENT = 10;
    private static final int BLOOM_HASH_COUNT = 4;

    private TreeMap<Integer, Boolean> memtable = new TreeMap<>();
    private final List<int[]> sstables = new ArrayList<>();
    private final List<BloomFilter> filters = new ArrayList<>();
    private long flushCount = 0;

    public void insert(int key) {
        memtable.put(key, true);
        if (memtable.size() >= MEMTABLE_FLUSH_THRESHOLD) {
            flush();
        }
    }

    private void flush() {
        int[] sorted = new int[memtable.size()];
        BloomFilter filter = new BloomFilter(memtable.size(), BLOOM_BITS_PER_ELEMENT, BLOOM_HASH_COUNT);
        int i = 0;
        for (int key : memtable.keySet()) {
            sorted[i++] = key;
            filter.add(key);
        }
        sstables.add(sorted);
        filters.add(filter);
        flushCount++;
        memtable = new TreeMap<>();
    }

    public long flushCount() {
        return flushCount;
    }

    public int sstableCount() {
        return sstables.size();
    }

    /**
     * Returns the number of *actual SSTable reads* performed -- i.e.
     * SSTables whose Bloom filter said "maybe present," so a real read
     * was required. SSTables the Bloom filter correctly ruled out never
     * get counted, because they were never actually read.
     */
    public int lookupCost(int key) {
        if (memtable.containsKey(key)) {
            return 0;
        }
        int actualReads = 0;
        for (int i = sstables.size() - 1; i >= 0; i--) {
            if (!filters.get(i).mightContain(key)) {
                continue; // Bloom filter says definitely not here -- skip, no read
            }
            actualReads++;
            if (binarySearch(sstables.get(i), key)) {
                return actualReads;
            }
        }
        return actualReads;
    }

    private boolean binarySearch(int[] sorted, int key) {
        int lo = 0;
        int hi = sorted.length - 1;
        while (lo <= hi) {
            int mid = (lo + hi) >>> 1;
            if (sorted[mid] == key) {
                return true;
            } else if (sorted[mid] < key) {
                lo = mid + 1;
            } else {
                hi = mid - 1;
            }
        }
        return false;
    }
}
