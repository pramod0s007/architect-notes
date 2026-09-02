import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<Document> docs = Arrays.asList(
            new Document("D1", "Product SKU X200 has a 2-year warranty covering manufacturing "
                + "defects only, excludes water damage.", 0.62),
            new Document("D2", "Our premium product comes with strong, comprehensive warranty "
                + "support and worry-free coverage for all issues.", 0.81),
            // A keyword-stuffed, near-content-free listing -- the kind of low-value page a
            // naive scraper or auto-generated catalog entry produces.
            new Document("D3", "X200 X200 X200 X200 catalog listing.", 0.15)
        );

        String query = "X200 warranty";
        System.out.println("Query: \"" + query + "\" -- hybrid: vector similarity + RAW keyword "
            + "occurrence count, added directly with no normalization:");

        Document best = null;
        double bestScore = -1;
        for (Document d : docs) {
            int rawCount = RawKeywordCounter.totalOccurrences(query, d.text);
            double score = d.vectorSimilarityToQuery + rawCount;
            System.out.printf("  %s: vector=%.2f, raw keyword occurrences=%d, fused score=%.2f%n",
                d.id, d.vectorSimilarityToQuery, rawCount, score);
            if (score > bestScore) {
                bestScore = score;
                best = d;
            }
        }

        System.out.println();
        System.out.println("Top result: " + best.id + " (fused score=" + String.format("%.2f", bestScore) + ")");
        System.out.println(best.id.equals("D3")
            ? "STILL FAILS, DIFFERENTLY: D3 -- a near-content-free listing that just repeats the SKU "
              + "four times -- wins, because raw occurrence count is unbounded while vector similarity "
              + "is capped at 1.0. Any document that repeats a query term enough times can out-score "
              + "genuine semantic relevance entirely. Unnormalized fusion doesn't balance the two "
              + "signals -- it lets whichever one has the larger numeric range dominate completely, "
              + "regardless of which one is actually more informative for a given query."
            : "Unexpected -- investigate.");
    }
}
