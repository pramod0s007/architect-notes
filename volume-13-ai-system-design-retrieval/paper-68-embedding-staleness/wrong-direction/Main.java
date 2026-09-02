import java.util.LinkedHashMap;
import java.util.Map;

public class Main {
    // The overcorrection: instead of re-embedding the whole index on a model
    // upgrade (expensive, all at once), re-embed a document only when it's
    // next edited -- a common, individually reasonable-sounding cost-saving
    // shortcut. D1 was recently edited (a typo fix) and got re-embedded with
    // V2 as a side effect. D2 and D3 haven't been touched since the model
    // upgrade, so they're still sitting on V1, indefinitely.
    public static void main(String[] args) {
        Map<String, double[]> mixedIndex = new LinkedHashMap<>();
        mixedIndex.put("D1 (refund policy, migrated on edit)",  new double[]{0.1, 0.85, 0.1});   // V2
        mixedIndex.put("D2 (shipping policy, never edited)",    new double[]{0.2, 1.0, 0.1});     // V1, stale
        mixedIndex.put("D3 (warranty policy, never edited)",    new double[]{0.1, 0.2, 1.0});      // V1, stale

        double[] queryAboutShipping = {0.05, 0.05, 0.9};

        System.out.println("Query: \"what is the shipping timeframe\" (embedded with V2)");
        String best = null;
        double bestScore = -2;
        for (Map.Entry<String, double[]> e : mixedIndex.entrySet()) {
            double score = CosineSimilarity.of(queryAboutShipping, e.getValue());
            System.out.printf("  %-38s similarity=%.4f%n", e.getKey(), score);
            if (score > bestScore) {
                bestScore = score;
                best = e.getKey();
            }
        }

        System.out.println();
        System.out.println("Top result: " + best + " (similarity=" + String.format("%.4f", bestScore) + ")");
        System.out.println(best.contains("D2")
            ? "Unexpected -- investigate."
            : "STILL FAILS: the correct answer (D2, shipping) was never edited, so it was never " +
              "migrated, so it's still sitting on the old, incompatible V1 embedding -- and it scores " +
              "far lower than an UNRELATED warranty document that also happens to still be on V1. " +
              "Migrating only what gets touched leaves everything else silently, indefinitely broken. " +
              "Most of a real knowledge base is old, unedited, and still perfectly valid content -- " +
              "exactly the content this strategy never fixes.");
    }
}
