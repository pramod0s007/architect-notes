import java.util.LinkedHashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        // The fix: on a model upgrade, every stored document is re-embedded with
        // the new model before it's ever compared against a query from that model.
        Map<String, double[]> indexV2 = new LinkedHashMap<>();
        indexV2.put("D1 (refund policy)",   new double[]{0.1, 0.85, 0.1});
        indexV2.put("D2 (shipping policy)", new double[]{0.1, 0.1, 0.85});
        indexV2.put("D3 (warranty policy)", new double[]{0.85, 0.1, 0.1});

        double[] queryAboutRefund = {0.05, 0.9, 0.05};

        System.out.println("Query: \"what is the refund timeframe\" (embedded with V2)");
        String best = null;
        double bestScore = -2;
        for (Map.Entry<String, double[]> e : indexV2.entrySet()) {
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
            ? "FIXED: the refund question now correctly retrieves the refund document, because " +
              "query and document are compared in the SAME model's coordinate system."
            : "Unexpected -- investigate.");
    }
}
