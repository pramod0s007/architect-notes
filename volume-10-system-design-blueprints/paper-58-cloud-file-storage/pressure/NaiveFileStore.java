import java.util.ArrayList;
import java.util.List;

// Stores every version of a file in full. Correct, and it charges full
// price for every version even when only a few bytes actually changed.
class NaiveFileStore {
    private final List<byte[]> versions = new ArrayList<>();

    void store(byte[] version) {
        versions.add(version);
    }

    long totalBytesStored() {
        long total = 0;
        for (byte[] v : versions) {
            total += v.length;
        }
        return total;
    }
}
