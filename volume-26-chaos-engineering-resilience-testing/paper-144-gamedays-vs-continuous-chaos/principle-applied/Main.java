/**
 * PRINCIPLE APPLIED: same underlying deploy timeline and same two regression windows as the
 * previous two scenarios, but the resilience check now runs continuously -- automated, against
 * every single deploy (every tick), not on a fixed calendar schedule. Because a check happens
 * on the same tick a regression is introduced, both regressions are caught the same modeled tick
 * they start, with zero detection delay -- there's no gap between scheduled checks for a
 * regression to hide in, because there's no gap between checks at all.
 */
public class Main {
    private static final int TOTAL_TICKS = 20;

    public static void main(String[] args) {
        boolean[] timeoutPresent = buildTimeline();

        System.out.println("Deploy timeline (tick: resilience mechanism present?):");
        for (int tick = 1; tick <= TOTAL_TICKS; tick++) {
            System.out.println("  Tick " + tick + ": " + (timeoutPresent[tick] ? "present" : "MISSING"));
        }

        int[] schedule = new int[TOTAL_TICKS];
        for (int i = 0; i < TOTAL_TICKS; i++) schedule[i] = i + 1;

        System.out.println();
        System.out.println("Automated-chaos schedule: continuous, automated chaos running against every deploy (every tick, 1 through "
                + TOTAL_TICKS + ").");

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
                + " -- every regression was caught the same tick it was introduced, because a check runs on every deploy");
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
