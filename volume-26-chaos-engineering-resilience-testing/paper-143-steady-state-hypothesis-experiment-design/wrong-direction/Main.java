import java.util.Random;

/**
 * WRONG DIRECTION: after the pressure scenario's uninterpretable single number, the team adds a
 * control group -- they now measure the baseline too -- and states a hypothesis: "success rate
 * should stay above 90%." But the 90% figure is a guessed, round-number threshold, not something
 * derived from the measured baseline, and it's applied identically (and separately) to both
 * series with no statistical comparison between them. This misses that the baseline itself
 * violates that same guessed threshold on a meaningful fraction of ticks, purely from natural
 * variance and zero fault -- so per-tick breaches against a guessed number can't distinguish a
 * real regression from noise the system already produces on its own.
 */
public class Main {
    private static final int TOTAL_TICKS = 20;
    private static final int REQUESTS_PER_TICK = 100;
    private static final int GUESSED_THRESHOLD = 90; // picked because it "seemed safe," not measured

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

        int baselineBreaches = 0;
        for (int v : baseline) if (v < GUESSED_THRESHOLD) baselineBreaches++;

        int faultBreaches = 0;
        for (int v : faultSeries) if (v < GUESSED_THRESHOLD) faultBreaches++;

        System.out.println("Control group (no fault), guessed threshold " + GUESSED_THRESHOLD + "%:");
        for (int tick = 1; tick <= TOTAL_TICKS; tick++) {
            int rate = baseline[tick - 1];
            String flag = rate < GUESSED_THRESHOLD ? " -- BELOW THRESHOLD" : "";
            if (tick <= 3 || !flag.isEmpty()) {
                System.out.println("  Tick " + tick + ": " + rate + "%" + flag);
            }
        }
        System.out.println("Control group breaches: " + baselineBreaches + " of " + TOTAL_TICKS
                + " ticks below threshold, with zero fault injected");

        System.out.println();
        System.out.println("Experiment group (fault injected), same guessed threshold:");
        for (int tick = 1; tick <= TOTAL_TICKS; tick++) {
            int rate = faultSeries[tick - 1];
            String flag = rate < GUESSED_THRESHOLD ? " -- BELOW THRESHOLD" : "";
            if (tick <= 3 || !flag.isEmpty()) {
                System.out.println("  Tick " + tick + ": " + rate + "%" + flag);
            }
        }
        System.out.println("Experiment group breaches: " + faultBreaches + " of " + TOTAL_TICKS + " ticks below threshold");

        System.out.println();
        System.out.println("Conclusion attempted: \"" + faultBreaches + " breaches means the fault caused a real regression.\" "
                + "But " + baselineBreaches + " of those same " + TOTAL_TICKS
                + " ticks would have breached the identical threshold with no fault present at all -- "
                + "the guessed threshold can't separate how much of the " + faultBreaches
                + " count is the fault versus how much is normal variance the system already produces.");
    }

    private static int clamp(int v) {
        return Math.max(0, Math.min(100, v));
    }
}
