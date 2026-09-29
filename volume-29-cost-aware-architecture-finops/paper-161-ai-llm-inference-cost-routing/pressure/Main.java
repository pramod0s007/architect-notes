import java.util.*;

/**
 * PRESSURE SCENARIO: every inference request routed to the large model, uniformly.
 *
 * A service answers user queries by sending every one of them to a single large
 * language model. This is the simplest possible routing policy -- there's no
 * decision to make, and every request is routed the same way regardless of its
 * category's own history. But the large model's per-query cost is ten times a
 * smaller model's, and a large share of requests are assigned a high historical
 * small-model accuracy in this demonstration's category data. Sending every
 * request to the large model assigns it that ten-times-higher per-query modeled
 * cost even for the queries whose own category history projects a cheaper route.
 */
public class Main {

    static final double LARGE_MODEL_COST_PER_QUERY = 0.02;

    static class QueryCategory {
        String name;
        int batchSize;

        QueryCategory(String name, int batchSize) {
            this.name = name;
            this.batchSize = batchSize;
        }
    }

    public static void main(String[] args) {
        List<QueryCategory> categories = new ArrayList<>();
        categories.add(new QueryCategory("simple-faq", 100));
        categories.add(new QueryCategory("complex-analysis", 100));

        System.out.println("-- Uniform policy: every query routed to the large model --");
        double totalCost = 0.0;
        for (QueryCategory c : categories) {
            double cost = c.batchSize * LARGE_MODEL_COST_PER_QUERY;
            totalCost += cost;
            System.out.printf("%-20s %d queries on LARGE model, $%.4f%n", c.name, c.batchSize, cost);
        }
        System.out.printf("Total: $%.4f%n", totalCost);

        System.out.println();
        System.out.println("-- What the uniform policy's modeled cost is for simple-faq, whose own history projects a cheaper route --");
        QueryCategory simpleFaq = categories.get(0);
        double modeledCost = simpleFaq.batchSize * LARGE_MODEL_COST_PER_QUERY;
        System.out.printf("simple-faq: $%.4f on the large model for %d queries in a category assigned a high historical small-model accuracy in this demonstration's data%n",
                modeledCost, simpleFaq.batchSize);
    }
}
