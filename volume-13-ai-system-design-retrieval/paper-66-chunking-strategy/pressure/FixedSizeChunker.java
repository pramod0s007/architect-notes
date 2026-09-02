import java.util.ArrayList;
import java.util.List;

// Splits text into fixed-size windows with no regard for sentence boundaries.
// The obvious first implementation of "chunking" for a RAG pipeline.
class FixedSizeChunker {
    private final int chunkSize;

    FixedSizeChunker(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    List<String> chunk(String text) {
        List<String> chunks = new ArrayList<>();
        for (int i = 0; i < text.length(); i += chunkSize) {
            chunks.add(text.substring(i, Math.min(i + chunkSize, text.length())));
        }
        return chunks;
    }
}
