import java.util.*;

/**
 * PRINCIPLE-APPLIED SCENARIO: per-category routing choice, from each category's own
 * historical small-model accuracy.
 *
 * Rather than routing by a proxy like word count, this policy compares two
 * candidate routes for each query category -- send it direct to the large model,
 * or try the small model first and retry with the large model only on failure --
 * using that category's own historical small-model accuracy to project each
 * route's cost, and keeps whichever route has the lower modeled cost for that
 * category. A category whose historical sample count is below a disclosed policy
 * cutoff (MIN_HISTORICAL_RUNS) is not routed through the small model while its
 * sample count is below that cutoff -- it goes direct to the large model until its
 * historical run count clears the cutoff.
 */
public class Main {

    static final double LARGE_MODEL_COST_PER_QUERY = 0.02;
    static final double SMALL_MODEL_COST_PER_QUERY = 0.002;
    static final int MIN_HISTORICAL_RUNS = 10;

    static class QueryCategory {
        String name;
        double historicalSmallModelAccuracy;
        int historicalRunCount;
        int batchSize;

        QueryCategory(String name, double historicalSmallModelAccuracy, int historicalRunCount, int batchSize) {
            this.name = name;
            this.historicalSmallModelAccuracy = historicalSmallModelAccuracy;
            this.historicalRunCount = historicalRunCount;
            this.batchSize = batchSize;
        }
    }

    static double directLargeCostPerQuery() {
        return LARGE_MODEL_COST_PER_QUERY;
    }

    static double cascadeCostPerQuery(QueryCategory c) {
        double failureRate = 1.0 - c.historicalSmallModelAccuracy;
        return SMALL_MODEL_COST_PER_QUERY + failureRate * LARGE_MODEL_COST_PER_QUERY;
    }

    public static void main(String[] args) {
        List<QueryCategory> categories = new ArrayList<>();
        categories.add(new QueryCategory("simple-faq", 0.98, 500, 100));
        categories.add(new QueryCategory("complex-analysis", 0.05, 400, 100));
        // new-diagnostic-category: only 3 historical samples so far -- below this
        // demonstration policy's disclosed raw-sample cutoff of MIN_HISTORICAL_RUNS,
        // regardless of how those 3 samples happened to score.
        categories.add(new QueryCategory("new-diagnostic-category", 1.00, 3, 20));

        System.out.println("-- Per-category lowest modeled-cost eligible route, chosen from each category's own history --");
        double totalCost = 0.0;
        for (QueryCategory c : categories) {
            if (c.historicalRunCount < MIN_HISTORICAL_RUNS) {
                double cost = directLargeCostPerQuery() * c.batchSize;
                totalCost += cost;
                System.out.printf("%-25s INSUFFICIENT_HISTORY (need >= %d samples, have %d) -- routed direct to LARGE, $%.4f%n",
                        c.name, MIN_HISTORICAL_RUNS, c.historicalRunCount, cost);
                continue;
            }
            double cascadeCost = cascadeCostPerQuery(c);
            double directCost = directLargeCostPerQuery();
            boolean cascadeWins = cascadeCost < directCost;
            double chosenCostPerQuery = cascadeWins ? cascadeCost : directCost;
            double totalForCategory = chosenCostPerQuery * c.batchSize;
            totalCost += totalForCategory;
            String route = cascadeWins ? "SMALL-then-retry-on-failure" : "direct LARGE";
            System.out.printf("%-25s lowest modeled-cost route: %s, $%.4f (history: %d samples, %.0f%% small-model accuracy)%n",
                    c.name, route, totalForCategory, c.historicalRunCount, c.historicalSmallModelAccuracy * 100);
        }
        System.out.printf("Total under per-category routing: $%.4f%n", totalCost);

        System.out.println();
        System.out.println("-- What the raw-sample-count policy cutoff changes for this category --");
        QueryCategory newCategory = categories.get(2);
        double naiveCascadeCost = cascadeCostPerQuery(newCategory) * newCategory.batchSize;
        double guardedCost = directLargeCostPerQuery() * newCategory.batchSize;
        System.out.printf("new-diagnostic-category: projecting off its 3 historical samples (100%% accuracy so far) would route SMALL-then-retry at $%.4f -- this policy instead routes direct to LARGE at $%.4f because its raw sample count is below the disclosed cutoff of %d, regardless of the recorded rate%n",
                naiveCascadeCost, guardedCost, MIN_HISTORICAL_RUNS);
    }
}
