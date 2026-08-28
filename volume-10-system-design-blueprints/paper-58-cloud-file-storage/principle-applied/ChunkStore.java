import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Only pays storage cost for chunks it hasn't seen before. Storing the same
// chunk fingerprint twice costs nothing -- the underlying bytes are already there.
class ChunkStore {
    private final Set<Integer> seenFingerprints = new HashSet<>();
    private long totalBytesStored = 0;

    int storeChunks(List<byte[]> chunks) {
        int newChunks = 0;
        for (byte[] chunk : chunks) {
            int fingerprint = Arrays.hashCode(chunk);
            if (seenFingerprints.add(fingerprint)) {
                totalBytesStored += chunk.length;
                newChunks++;
            }
        }
        return newChunks;
    }

    long totalBytesStored() {
        return totalBytesStored;
    }
}
