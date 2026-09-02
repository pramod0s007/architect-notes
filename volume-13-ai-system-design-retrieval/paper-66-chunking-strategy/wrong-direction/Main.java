import java.util.List;

public class Main {
    static final String POLICY_DOC =
        "Section 4: Returns and Refunds. Any item purchased through the online store may be returned " +
        "within a reasonable period for a full refund, provided it is unused and in original packaging. " +
        "The refund window is thirty days from the date of purchase confirmation, not the delivery date, " +
        "which is a common point of confusion for support agents. After thirty days, only store credit is " +
        "issued, and no cash or card refunds are available under any circumstances.";

    public static void main(String[] args) {
        FixedSizeChunkerNoOverlapForComparison baseline = new FixedSizeChunkerNoOverlapForComparison(207);
        int baselineCount = baseline.chunk(POLICY_DOC).size();

        SlidingWindowChunker chunker = new SlidingWindowChunker(207, 100);
        List<String> chunks = chunker.chunk(POLICY_DOC);

        System.out.println("Baseline fixed-size (no overlap): " + baselineCount + " chunks.");
        System.out.println("Sliding-window (100-char overlap): " + chunks.size() + " chunks " +
            "(" + (chunks.size() - baselineCount) + " more, pure storage/embedding-cost overhead).");
        for (int i = 0; i < chunks.size(); i++) {
            System.out.println("chunk " + i + ": " + chunks.get(i));
        }

        String query = "what is the refund window";
        String best = new KeywordRetriever().bestChunk(chunks, query);
        System.out.println();
        System.out.println("Query: \"" + query + "\"");
        System.out.println("Retrieved chunk: " + best);

        boolean hasAnswer = best.contains("thirty days");
        System.out.println("Retrieved chunk contains the actual answer (\"thirty days\"): " + hasAnswer);
        System.out.println(hasAnswer
            ? "Unexpected -- investigate."
            : "STILL FAILS: chunk 0 -- the exact same broken fragment fixed-size chunking produced -- " +
              "is untouched by the overlap, because the window still advances in fixed-size steps with " +
              "no idea where the sentence ends. Overlap adds MORE chunks (more storage, more embedding " +
              "cost) without removing the one defective chunk that caused the original failure. " +
              "A later, overlapping chunk happens to contain the full phrase too -- but naive top-1 " +
              "retrieval still latches onto the higher keyword-density fragment. Overlap dilutes the " +
              "problem; it does not fix it.");
    }
}

// Local copy of the pressure section's chunker, used here only as a chunk-count
// baseline for comparison -- not part of the fix, not part of the failure.
class FixedSizeChunkerNoOverlapForComparison {
    private final int chunkSize;

    FixedSizeChunkerNoOverlapForComparison(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    List<String> chunk(String text) {
        java.util.List<String> chunks = new java.util.ArrayList<>();
        for (int i = 0; i < text.length(); i += chunkSize) {
            chunks.add(text.substring(i, Math.min(i + chunkSize, text.length())));
        }
        return chunks;
    }
}
