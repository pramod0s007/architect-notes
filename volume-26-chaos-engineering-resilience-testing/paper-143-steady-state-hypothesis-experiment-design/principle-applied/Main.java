import java.util.Random;

/**
 * PRINCIPLE APPLIED: same control group and same fault-injected experiment group as the
 * wrong-direction scenario, but the steady-state hypothesis is now framed relative to the
 * control group's own measured mean, with an explicitly stated tolerance -- not a guessed
 * absolute number. The hypothesis: "the experiment group's mean success rate should stay no
 * more than TOLERANCE_POINTS below the control group's measured mean." Because the reference
 * point is the actual measured baseline instead of a round number picked in advance, this
 * identifies the fault's effect as a deviation beyond what the system's own natural variance
 * produces (relative to a stated tolerance, not a formal statistical significance test).
 */
public class Main {
    private static final int TOTAL_TICKS = 20;
    private static final int REQUESTS_PER_TICK = 100;
    private static final double TOLERANCE_POINTS = 2.0; // stated hypothesis: no more than 2 points below measured baseline mean

    public static void main(String[] args) {
        Random rnd = new Random(42L);

        int[] baseline = new int[TOTAL_TICKS];
        for (int i = 0; i < TOTAL_TICKS; i++) {
            int noise = rnd.nextInt(13) - 6;
            baseline[i] = clamp(94 + noise);
        }

        int[] faultSeries = new int[TOTAL_TICKS];
        for (int i = 0; i < TOTAL_TICKS; i++) {
            int noise = rnd.nextInt(13) - 6;
            faultSeries[i] = clamp(89 + noise);
        }

        double baselineMean = mean(baseline);
        double faultMean = mean(faultSeries);
        double floor = baselineMean - TOLERANCE_POINTS;

        System.out.println("Control group measured mean: " + String.format("%.2f", baselineMean) + "%"
                + " (this is the actual reference point, not a guessed number)");
        System.out.println("Steady-state hypothesis: experiment-group mean stays at or above "
                + String.format("%.2f", floor) + "% (baseline mean minus " + TOLERANCE_POINTS + " points)");
        System.out.println();
        System.out.println("Experiment group measured mean: " + String.format("%.2f", faultMean) + "%");

        boolean violated = faultMean < floor;
        System.out.println();
        if (violated) {
            System.out.println("Hypothesis violated: " + String.format("%.2f", faultMean)
                    + "% is " + String.format("%.2f", floor - faultMean)
                    + " points below the " + String.format("%.2f", floor)
                    + "% floor derived from the measured baseline -- a deviation beyond the stated tolerance, "
                    + "distinguishable from the baseline's own natural variance because it's measured against that baseline directly "
                    + "(a simplified illustrative comparison, not a formal statistical significance test)");
        } else {
            System.out.println("Hypothesis holds: experiment-group mean is no more than " + TOLERANCE_POINTS
                    + " points below the measured baseline");
        }
    }

    private static double mean(int[] values) {
        long sum = 0;
        for (int v : values) sum += v;
        return sum / (double) values.length;
    }

    private static int clamp(int v) {
        return Math.max(0, Math.min(100, v));
    }
}
