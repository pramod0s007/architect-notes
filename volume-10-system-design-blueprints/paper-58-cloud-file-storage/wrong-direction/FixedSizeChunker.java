import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Chunk boundaries are fixed byte offsets: 0-127, 128-255, 256-383, and so
// on, regardless of content. Looks like the same idea as content-defined
// chunking -- split the file, hash each piece, dedup identical pieces.
class FixedSizeChunker {
    private static final int CHUNK_SIZE = 128;

    List<byte[]> chunk(byte[] data) {
        List<byte[]> chunks = new ArrayList<>();
        for (int i = 0; i < data.length; i += CHUNK_SIZE) {
            int end = Math.min(i + CHUNK_SIZE, data.length);
            chunks.add(Arrays.copyOfRange(data, i, end));
        }
        return chunks;
    }
}
