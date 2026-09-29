import java.util.Random;

/**
 * PRESSURE: "just inject the fault and see what happens" -- no steady-state baseline is ever
 * measured, only the fault-injected experiment group's own numbers are collected. A fixed seed
 * (42L) is used purely so this run is reproducible; the team in this story never sees a control
 * group at all, only the raw experiment-group success rate per tick.
 */
public class Main {
    private static final int TOTAL_TICKS = 20;
    private static final int REQUESTS_PER_TICK = 100;

    public static void main(String[] args) {
        Random rnd = new Random(42L);

        // Draw order matters: the control-group (baseline) draws are consumed first, then the
        // experiment-group (fault) draws, identically in every scenario in this paper, so the
        // underlying world being observed is bit-for-bit the same across all three -- only what
        // gets measured and how it's interpreted differs.
        int[] baseline = new int[TOTAL_TICKS];
        for (int i = 0; i < TOTAL_TICKS; i++) {
            int noise = rnd.nextInt(13) - 6; // natural variance: -6..+6 percentage points
            baseline[i] = clamp(94 + noise);
        }

        int[] faultSeries = new int[TOTAL_TICKS];
        for (int i = 0; i < TOTAL_TICKS; i++) {
            int noise = rnd.nextInt(13) - 6; // same natural variance shape, applied on top of a real regression
            faultSeries[i] = clamp(89 + noise);
        }

        // This scenario never measures or reports "baseline" at all -- it's generated above only
        // to keep the fault series' random draws identical to the other two scenarios.
        long sum = 0;
        for (int tick = 1; tick <= TOTAL_TICKS; tick++) {
            int rate = faultSeries[tick - 1];
            sum += rate;
            System.out.println("Tick " + tick + ": success rate " + rate + "%");
        }

        double mean = sum / (double) TOTAL_TICKS;
        System.out.println();
        System.out.println("Experiment-group mean success rate: " + String.format("%.1f", mean)
                + "% -- no baseline was ever measured, so there's no reference point for whether this number is normal or a real regression");
    }

    private static int clamp(int v) {
        return Math.max(0, Math.min(100, v));
    }
}
