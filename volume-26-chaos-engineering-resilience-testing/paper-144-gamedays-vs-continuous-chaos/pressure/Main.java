/**
 * PRESSURE: "we ran a GameDay, resilience is verified" -- a single one-time GameDay runs once,
 * at tick 1, and is never run again. Two real regressions to the same resilience mechanism (a
 * timeout) are introduced later in the deploy timeline. Because the only GameDay this system will
 * ever run already happened before either regression existed, neither is ever caught by testing --
 * only a real incident that happens to depend on the missing mechanism would ever reveal them.
 */
public class Main {
    private static final int TOTAL_TICKS = 20;

    public static void main(String[] args) {
        boolean[] timeoutPresent = buildTimeline();

        System.out.println("Deploy timeline (tick: resilience mechanism present?):");
        for (int tick = 1; tick <= TOTAL_TICKS; tick++) {
            System.out.println("  Tick " + tick + ": " + (timeoutPresent[tick] ? "present" : "MISSING"));
        }

        int[] schedule = {1};

        System.out.println();
        System.out.println("GameDay schedule: a single one-time GameDay, run once at tick 1, never again.");

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
                + " -- the only GameDay this system will ever run already happened, before either regression existed");
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
