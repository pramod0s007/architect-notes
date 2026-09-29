/**
 * WRONG DIRECTION: after the pressure scenario's single one-time GameDay, the team improves the
 * practice by re-running the same GameDay on a fixed periodic schedule -- every 5 ticks. Same
 * underlying deploy timeline, same two regression windows as the pressure scenario. Periodic
 * re-running catches a regression that happens to still be live when a scheduled check lands, but
 * a regression that starts and ends entirely inside this specific 5-tick cadence's gap is
 * invisible to it -- a different cadence could close this particular gap, but only by shrinking
 * it, not by removing the practical labor ceiling on how often a human-run exercise can happen.
 */
public class Main {
    private static final int TOTAL_TICKS = 20;

    public static void main(String[] args) {
        boolean[] timeoutPresent = buildTimeline();

        System.out.println("Deploy timeline (tick: resilience mechanism present?):");
        for (int tick = 1; tick <= TOTAL_TICKS; tick++) {
            System.out.println("  Tick " + tick + ": " + (timeoutPresent[tick] ? "present" : "MISSING"));
        }

        int[] schedule = {1, 6, 11, 16};

        System.out.println();
        System.out.println("GameDay schedule: re-run on a fixed periodic schedule, every 5 ticks (ticks 1, 6, 11, 16).");

        int[][] windows = findRegressionWindows(timeoutPresent);
        int detected = 0;
        for (int[] w : windows) {
            int start = w[0], end = w[1];
            int firstCatch = -1;
            for (int t : schedule) {
                if (t >= start && t <= end) { firstCatch = t; break; }
            }
            if (firstCatch != -1) {
                detected++;
                System.out.println("  Regression window [" + start + "-" + end + "]: caught at tick " + firstCatch
                        + " (" + (firstCatch - start) + "-tick detection delay)");
            } else {
                System.out.println("  Regression window [" + start + "-" + end + "]: MISSED -- no scheduled check ever fell inside this window");
            }
        }

        System.out.println();
        System.out.println("Regressions detected by testing: " + detected + " of " + windows.length
                + " -- periodic re-running caught the regression that was still live at a scheduled check, "
                + "but missed the one that started and ended entirely between two scheduled checks");
    }

    private static boolean[] buildTimeline() {
        boolean[] present = new boolean[TOTAL_TICKS + 1];
        for (int tick = 1; tick <= TOTAL_TICKS; tick++) {
            boolean regressionA = tick >= 7 && tick <= 9;   // timeout accidentally removed by an unrelated refactor, ticks 7-9
            boolean regressionB = tick >= 13;               // timeout removed again, tick 13 onward, never restored in this run
            present[tick] = !(regressionA || regressionB);
        }
        return present;
    }

    private static int[][] findRegressionWindows(boolean[] present) {
        java.util.List<int[]> windows = new java.util.ArrayList<>();
        int start = -1;
        for (int tick = 1; tick <= TOTAL_TICKS; tick++) {
            if (!present[tick] && start == -1) {
                start = tick;
            } else if (present[tick] && start != -1) {
                windows.add(new int[]{start, tick - 1});
                start = -1;
            }
        }
        if (start != -1) windows.add(new int[]{start, TOTAL_TICKS});
        return windows.toArray(new int[0][]);
    }
}
