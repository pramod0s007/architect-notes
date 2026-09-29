import java.util.*;

/**
 * WRONG-DIRECTION SCENARIO: routing by a word-count proxy for historical small-model accuracy.
 *
 * The fix for the pressure scenario's uniform overspend looks obvious: route
 * short queries to a cheap small model, and only use the large model for long
 * queries. Word count is used as the proxy for a category's own historical
 * small-model accuracy, on the theory that short queries correlate with a
 * high historical small-model accuracy and long queries correlate with a low
 * one. But word count and this category's own historical small-model accuracy
 * are two different signals -- a long-winded category with a high historical
 * small-model accuracy, and a short category with a low one, can both defeat
 * this proxy in opposite directions. When the small model's answer fails
 * validation, this system always retries with the large model rather than
 * returning that answer, so a query routed to the small model based on a proxy
 * that misreads this category's own history is assigned the modeled cost of both
 * the small-model attempt and the large-model retry that follows it.
 */
public class Main {

    static final double LARGE_MODEL_COST_PER_QUERY = 0.02;
    static final double SMALL_MODEL_COST_PER_QUERY = 0.002;
    static final int WORD_COUNT_THRESHOLD = 20; // queries shorter than this go to the small model first

    static class QueryCategory {
        String name;
        int wordCount;
        double historicalSmallModelAccuracy; // this category's own historical fraction of small-model answers that passed validation on the first try
        int batchSize;

        QueryCategory(String name, int wordCount, double historicalSmallModelAccuracy, int batchSize) {
            this.name = name;
            this.wordCount = wordCount;
            this.historicalSmallModelAccuracy = historicalSmallModelAccuracy;
            this.batchSize = batchSize;
        }
    }

    // Disclosed simplifying assumption: the large model is treated as always passing
    // validation on the first try, so a query that reaches the large model never needs a
    // further retry in this model.
    static double directLargeCostPerQuery() {
        return LARGE_MODEL_COST_PER_QUERY;
    }

    static double cascadeCostPerQuery(QueryCategory c) {
        double failureRate = 1.0 - c.historicalSmallModelAccuracy;
        return SMALL_MODEL_COST_PER_QUERY + failureRate * LARGE_MODEL_COST_PER_QUERY;
    }

    public static void main(String[] args) {
        List<QueryCategory> categories = new ArrayList<>();
        categories.add(new QueryCategory("simple-faq", 45, 0.98, 100));
        categories.add(new QueryCategory("complex-analysis", 15, 0.05, 100));

        System.out.println("-- Word-count proxy: queries under " + WORD_COUNT_THRESHOLD + " words go to the small model first, others go direct to the large model --");
        double totalCost = 0.0;
        for (QueryCategory c : categories) {
            boolean routedToSmallFirst = c.wordCount < WORD_COUNT_THRESHOLD;
            double costPerQuery = routedToSmallFirst ? cascadeCostPerQuery(c) : directLargeCostPerQuery();
            double totalForCategory = costPerQuery * c.batchSize;
            totalCost += totalForCategory;
            String route = routedToSmallFirst ? "SMALL-then-retry-on-failure" : "direct LARGE";
            System.out.printf("%-20s %d words, %d queries, routed %s, $%.4f%n",
                    c.name, c.wordCount, c.batchSize, route, totalForCategory);
        }
        System.out.printf("Total under word-count routing: $%.4f%n", totalCost);

        System.out.println();
        System.out.println("-- Where the word-count proxy's modeled cost exceeds a direct-large route or a same-category cascade --");
        QueryCategory complexAnalysis = categories.get(1);
        double modeledCost = cascadeCostPerQuery(complexAnalysis) * complexAnalysis.batchSize;
        double directCost = directLargeCostPerQuery() * complexAnalysis.batchSize;
        System.out.printf("complex-analysis: $%.4f routed SMALL-then-retry (its own historical small-model accuracy is only %.0f%%) vs $%.4f if routed direct to LARGE -- this category's short word count put it under the threshold despite its low historical small-model accuracy, and this policy's modeled cost is $%.4f higher than direct-large's would have been%n",
                modeledCost, complexAnalysis.historicalSmallModelAccuracy * 100, directCost, modeledCost - directCost);
    }
}
