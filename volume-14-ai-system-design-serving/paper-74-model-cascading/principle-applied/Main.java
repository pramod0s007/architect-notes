import java.util.Arrays;
import java.util.List;

public class Main {
    static final double CHEAP_MODEL_COST = 0.001;
    static final double EXPENSIVE_MODEL_COST = 0.03;

    public static void main(String[] args) {
        List<Query> queries = Arrays.asList(
            new Query("Q1", "SIMPLE", 0.95, true),
            new Query("Q2", "SIMPLE", 0.90, true),
            new Query("Q3", "COMPLEX", 0.92, false),
            new Query("Q4", "COMPLEX", 0.35, false)
        );

        // Route by a known, pre-classified query category -- decided BEFORE either
        // model ever answers, not by asking the cheap model how sure it feels
        // about its own answer.
        double totalCost = 0;
        int correct = 0;
        System.out.println("Routed by known query category (decided before either model answers):");
        for (Query q : queries) {
            boolean useCheap = q.category.equals("SIMPLE");
            double cost = useCheap ? CHEAP_MODEL_COST : EXPENSIVE_MODEL_COST;
            totalCost += cost;
            // SIMPLE queries are, by category definition, within the cheap model's
            // verified competence; COMPLEX queries go to the expensive model, which
            // is assumed correct for this comparison.
            boolean gotCorrectAnswer = useCheap ? q.cheapModelActuallyCorrect : true;
            if (gotCorrectAnswer) correct++;
            System.out.println("  " + q.id + " (" + q.category + "): "
                + (useCheap ? "cheap" : "expensive") + " model, cost=$" + cost
                + ", correct=" + gotCorrectAnswer);
        }

        System.out.println();
        System.out.println("Total cost: $" + String.format("%.3f", totalCost) + ", correct: " + correct + "/" + queries.size());
        double pressureBaseline = 4 * EXPENSIVE_MODEL_COST;
        System.out.println("vs. always-expensive: $" + pressureBaseline + " -- "
            + Math.round(100 - (totalCost / pressureBaseline * 100)) + "% cost reduction, same correctness.");
    }
}
