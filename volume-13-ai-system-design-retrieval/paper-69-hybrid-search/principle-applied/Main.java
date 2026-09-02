import java.util.Arrays;
import java.util.List;

public class Main {
    static final double EXACT_MATCH_BOOST = 0.2;

    public static void main(String[] args) {
        List<Document> docs = Arrays.asList(
            new Document("D1", "Product SKU X200 has a 2-year warranty covering manufacturing "
                + "defects only, excludes water damage.", 0.62),
            new Document("D2", "Our premium product comes with strong, comprehensive warranty "
                + "support and worry-free coverage for all issues.", 0.81),
            new Document("D3", "X200 X200 X200 X200 catalog listing.", 0.15)
        );

        String query = "X200 warranty";
        System.out.println("Query: \"" + query + "\" -- hybrid: vector similarity + a bounded, "
            + "same-scale boost when EVERY query term is present:");

        Document best = null;
        double bestScore = -1;
        for (Document d : docs) {
            boolean allTermsPresent = KeywordMatcher.containsAllTerms(query, d.text);
            double score = d.vectorSimilarityToQuery + (allTermsPresent ? EXACT_MATCH_BOOST : 0.0);
            System.out.printf("  %s: vector=%.2f, all terms present=%b, fused score=%.2f%n",
                d.id, d.vectorSimilarityToQuery, allTermsPresent, score);
            if (score > bestScore) {
                bestScore = score;
                best = d;
            }
        }

        System.out.println();
        System.out.println("Top result: " + best.id + " (fused score=" + String.format("%.2f", bestScore) + ")");
        System.out.println(best.id.equals("D1")
            ? "FIXED: D1 -- the document containing every term the user actually typed -- now wins, " +
              "by a margin the boost was sized to close (0.82 vs D2's 0.81) without needing to swamp " +
              "the semantic signal entirely."
            : "Unexpected -- investigate.");
    }
}
