import java.util.LinkedHashMap;
import java.util.Map;

public class Main {
    // Two embedding models, standing in for "the model the knowledge base was
    // originally built with" (V1) and "the model that replaced it after a
    // retraining" (V2). Both are legitimate, useful embedding spaces on their
    // own -- they just don't agree on what each coordinate means, because they
    // were trained independently. V1's axes are [refund, shipping, warranty].
    // V2's axes are [warranty, refund, shipping] -- a different model organizes
    // the same three concepts along different dimensions.

    public static void main(String[] args) {
        // Documents embedded at index-build time, using V1 -- never re-embedded
        // since (this is the pressure: the model was upgraded, the documents weren't).
        Map<String, double[]> indexV1 = new LinkedHashMap<>();
        indexV1.put("D1 (refund policy)",   new double[]{1.0, 0.2, 0.1});
        indexV1.put("D2 (shipping policy)", new double[]{0.2, 1.0, 0.1});
        indexV1.put("D3 (warranty policy)", new double[]{0.1, 0.2, 1.0});

        // A live query, embedded with the CURRENT model (V2) -- every query goes
        // through whatever model is live today, regardless of what embedded the
        // documents it's being compared against.
        double[] queryAboutRefund = {0.05, 0.9, 0.05};

        System.out.println("Query: \"what is the refund timeframe\" (embedded with V2)");
        String best = null;
        double bestScore = -2;
        for (Map.Entry<String, double[]> e : indexV1.entrySet()) {
            double score = CosineSimilarity.of(queryAboutRefund, e.getValue());
            System.out.printf("  %-22s similarity=%.4f%n", e.getKey(), score);
            if (score > bestScore) {
                bestScore = score;
                best = e.getKey();
            }
        }

        System.out.println();
        System.out.println("Top result: " + best + " (similarity=" + String.format("%.4f", bestScore) + ")");
        System.out.println(best.contains("D1")
            ? "Unexpected -- investigate."
            : "CONFIRMED FAILURE: the top result for a refund question is NOT the refund document. " +
              "V1's and V2's coordinate systems don't correspond to the same concepts, so comparing a " +
              "V2 query against V1 documents produces a similarity score that means nothing -- it just " +
              "happens to be numerically high here, by coincidence of which axis lines up with which.");
    }
}
