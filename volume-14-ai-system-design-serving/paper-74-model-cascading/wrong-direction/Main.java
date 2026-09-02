import java.util.Arrays;
import java.util.List;

public class Main {
    static final double CHEAP_MODEL_COST = 0.001;
    static final double EXPENSIVE_MODEL_COST = 0.03;
    static final double CONFIDENCE_THRESHOLD = 0.8;

    public static void main(String[] args) {
        List<Query> queries = Arrays.asList(
            new Query("Q1", "SIMPLE", 0.95, true),
            new Query("Q2", "SIMPLE", 0.90, true),
            new Query("Q3", "COMPLEX", 0.92, false),
            new Query("Q4", "COMPLEX", 0.35, false)
        );

        // The overcorrection: route by the cheap model's OWN self-reported
        // confidence instead of a known, verified query category. This sounds
        // more adaptive -- the model decides for itself when it needs help.
        double totalCost = 0;
        int correct = 0;
        System.out.println("Routed by the cheap model's own self-reported confidence (threshold=" + CONFIDENCE_THRESHOLD + "):");
        for (Query q : queries) {
            boolean useCheap = q.cheapModelConfidence >= CONFIDENCE_THRESHOLD;
            double cost = useCheap ? CHEAP_MODEL_COST : EXPENSIVE_MODEL_COST;
            totalCost += cost;
            boolean gotCorrectAnswer = useCheap ? q.cheapModelActuallyCorrect : true;
            if (gotCorrectAnswer) correct++;
            System.out.println("  " + q.id + " (" + q.category + ", confidence=" + q.cheapModelConfidence + "): "
                + (useCheap ? "cheap" : "expensive") + " model, cost=$" + cost
                + ", correct=" + gotCorrectAnswer);
        }

        System.out.println();
        System.out.println("Total cost: $" + String.format("%.3f", totalCost) + ", correct: " + correct + "/" + queries.size());
        System.out.println(correct < queries.size()
            ? "STILL FAILS, MORE CHEAPLY: Q3 was confidently (0.92) answered wrong by the cheap model, "
              + "and confidence alone never triggered an escalation -- the wrong answer was served with "
              + "no error, no warning, and a LOWER total cost than the category-based routing above. "
              + "Self-reported confidence isn't calibrated: a model can be exactly as sure about a wrong "
              + "answer as a right one, and routing on confidence alone bets the entire escalation "
              + "decision on a signal that was never verified to correlate with actually being correct."
            : "Unexpected -- investigate.");
    }
}
