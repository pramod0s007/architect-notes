import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Chunk boundaries are decided by a rolling hash over a sliding window of
// the CONTENT itself, not by fixed byte offsets. Because a boundary only
// depends on the bytes immediately around it, an edit anywhere in the file
// only disturbs the chunks near that edit -- every chunk after the next
// naturally-occurring boundary re-syncs and looks identical to before.
class ContentDefinedChunker {
    private static final int WINDOW_SIZE = 16;
    private static final int MIN_CHUNK = 64;
    private static final int MAX_CHUNK = 512;
    private static final int TARGET_CHUNK = 128; // average chunk size

    List<byte[]> chunk(byte[] data) {
        List<byte[]> chunks = new ArrayList<>();
        int start = 0;
        int rollingSum = 0;

        for (int i = 0; i < data.length; i++) {
            rollingSum += (data[i] & 0xFF);
            if (i - start >= WINDOW_SIZE) {
                rollingSum -= (data[i - WINDOW_SIZE] & 0xFF);
            }
            int chunkLen = i - start + 1;
            boolean atBoundary = (rollingSum % TARGET_CHUNK) == 0;
            if (chunkLen >= MIN_CHUNK && (atBoundary || chunkLen >= MAX_CHUNK)) {
                chunks.add(Arrays.copyOfRange(data, start, i + 1));
                start = i + 1;
                rollingSum = 0;
            }
        }
        if (start < data.length) {
            chunks.add(Arrays.copyOfRange(data, start, data.length));
        }
        return chunks;
    }
}
