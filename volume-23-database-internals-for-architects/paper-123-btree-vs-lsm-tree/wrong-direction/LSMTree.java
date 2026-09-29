import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * An LSM-tree style storage engine with no bloom filters: writes are
 * always sequential appends into an in-memory memtable, which gets
 * flushed as an immutable, sorted SSTable once it reaches a size
 * threshold. This makes writes dramatically cheaper than a B-Tree's
 * scattered node writes -- but a lookup that isn't in the memtable has
 * no way to know which (if any) SSTable holds the key, so it has to
 * check every single SSTable, newest to oldest, until it finds the key
 * or runs out of SSTables to check.
 */
public class LSMTree {
    private static final int MEMTABLE_FLUSH_THRESHOLD = 500;

    private TreeMap<Integer, Boolean> memtable = new TreeMap<>();
    private final List<int[]> sstables = new ArrayList<>(); // each is a sorted array of keys
    private long flushCount = 0;

    public void insert(int key) {
        memtable.put(key, true);
        if (memtable.size() >= MEMTABLE_FLUSH_THRESHOLD) {
            flush();
        }
    }

    private void flush() {
        int[] sorted = new int[memtable.size()];
        int i = 0;
        for (int key : memtable.keySet()) {
            sorted[i++] = key;
        }
        sstables.add(sorted); // one sequential write of the whole sorted run
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
     * Returns the number of SSTables actually read to answer this
     * lookup. The memtable is checked first (in-memory, not a disk
     * read); if the key isn't there, every SSTable from newest to
     * oldest must be read, because nothing indicates which ones might
     * contain the key.
     */
    public int lookupCost(int key) {
        if (memtable.containsKey(key)) {
            return 0; // found in memory, no SSTable read needed
        }
        int reads = 0;
        for (int i = sstables.size() - 1; i >= 0; i--) {
            reads++;
            if (binarySearch(sstables.get(i), key)) {
                return reads;
            }
        }
        return reads;
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
