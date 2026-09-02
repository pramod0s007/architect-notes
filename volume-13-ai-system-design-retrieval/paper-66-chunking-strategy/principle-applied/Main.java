import java.util.List;

public class Main {
    static final String POLICY_DOC =
        "Section 4: Returns and Refunds. Any item purchased through the online store may be returned " +
        "within a reasonable period for a full refund, provided it is unused and in original packaging. " +
        "The refund window is thirty days from the date of purchase confirmation, not the delivery date, " +
        "which is a common point of confusion for support agents. After thirty days, only store credit is " +
        "issued, and no cash or card refunds are available under any circumstances.";

    public static void main(String[] args) {
        SentenceAwareChunker chunker = new SentenceAwareChunker(207);
        List<String> chunks = chunker.chunk(POLICY_DOC);

        System.out.println("Document split into " + chunks.size() + " sentence-aware chunks.");
        for (int i = 0; i < chunks.size(); i++) {
            System.out.println("chunk " + i + " (" + chunks.get(i).length() + " chars): " + chunks.get(i));
        }

        String query = "what is the refund window";
        String best = new KeywordRetriever().bestChunk(chunks, query);
        System.out.println();
        System.out.println("Query: \"" + query + "\"");
        System.out.println("Retrieved chunk: " + best);

        boolean hasAnswer = best.contains("thirty days");
        System.out.println("Retrieved chunk contains the actual answer (\"thirty days\"): " + hasAnswer);
        System.out.println(hasAnswer
            ? "FIXED: the sentence that answers the question was never split, so the highest-scoring " +
              "chunk is also the one that actually contains the answer."
            : "Unexpected -- investigate.");
    }
}
