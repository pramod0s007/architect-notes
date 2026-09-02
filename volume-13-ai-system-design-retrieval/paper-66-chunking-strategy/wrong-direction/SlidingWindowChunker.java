import java.util.ArrayList;
import java.util.List;

// The overcorrection: instead of respecting sentence boundaries, keep fixed-size
// windows but overlap them, on the theory that overlap "catches" whatever a hard
// boundary cuts off. The window still advances in fixed-size steps -- it never
// looks at where a sentence actually ends.
class SlidingWindowChunker {
    private final int chunkSize;
    private final int overlap;

    SlidingWindowChunker(int chunkSize, int overlap) {
        this.chunkSize = chunkSize;
        this.overlap = overlap;
    }

    List<String> chunk(String text) {
        List<String> chunks = new ArrayList<>();
        int step = chunkSize - overlap;
        for (int i = 0; i < text.length(); i += step) {
            chunks.add(text.substring(i, Math.min(i + chunkSize, text.length())));
        }
        return chunks;
    }
}
