import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<Document> docs = Arrays.asList(
            new Document("D1", "Product SKU X200 has a 2-year warranty covering manufacturing "
                + "defects only, excludes water damage.", 0.62),
            new Document("D2", "Our premium product comes with strong, comprehensive warranty "
                + "support and worry-free coverage for all issues.", 0.81),
            new Document("D3", "X200 X200 X200 X200 catalog listing.", 0.15)
        );

        String query = "X200 warranty";
        System.out.println("Query: \"" + query + "\" -- pure vector (semantic) search, ranked by similarity alone:");

        Document best = null;
        for (Document d : docs) {
            System.out.printf("  %s: vector similarity=%.2f%n", d.id, d.vectorSimilarityToQuery);
            if (best == null || d.vectorSimilarityToQuery > best.vectorSimilarityToQuery) {
                best = d;
            }
        }

        System.out.println();
        System.out.println("Top result: " + best.id + " (similarity=" + best.vectorSimilarityToQuery + ")");
        System.out.println(best.id.equals("D1")
            ? "Unexpected -- investigate."
            : "CONFIRMED FAILURE: the user explicitly typed \"X200\" -- an exact product code -- and " +
              "the top result doesn't even mention it. D2's broad, generic warranty language embeds " +
              "more strongly on the general concept of \"warranty\" than D1's narrower, more specific " +
              "text does, so pure semantic similarity ranks it higher, even though D1 is the one " +
              "document that actually matches what the user typed.");
    }
}
