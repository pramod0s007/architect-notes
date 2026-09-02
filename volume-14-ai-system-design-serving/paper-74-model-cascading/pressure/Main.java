import java.util.Arrays;
import java.util.List;

public class Main {
    // Illustrative per-query cost figure, standing in for a real expensive-model
    // pricing point -- not a claim about any specific vendor's actual prices.
    static final double EXPENSIVE_MODEL_COST = 0.03;

    public static void main(String[] args) {
        List<Query> queries = Arrays.asList(
            new Query("Q1", "SIMPLE", 0.95, true),
            new Query("Q2", "SIMPLE", 0.90, true),
            new Query("Q3", "COMPLEX", 0.92, false),
            new Query("Q4", "COMPLEX", 0.35, false)
        );

        double totalCost = 0;
        int correct = 0;
        System.out.println("Every query routed to the expensive model, always:");
        for (Query q : queries) {
            totalCost += EXPENSIVE_MODEL_COST;
            correct++; // the expensive model is assumed correct for this comparison
            System.out.println("  " + q.id + " (" + q.category + "): expensive model, cost=$"
                + EXPENSIVE_MODEL_COST);
        }

        System.out.println();
        System.out.println("Total cost: $" + totalCost + ", correct: " + correct + "/" + queries.size());
        System.out.println("CONFIRMED: every query pays the expensive model's cost, including the ones "
            + "simple enough that a much cheaper model would have answered them correctly anyway.");
    }
}
