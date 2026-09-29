/**
 * PRESSURE: "we have a documented DR plan, it's fine" -- the failover plan is reviewed on paper
 * and never actually exercised. The plan documents an expected replication lag of 2 minutes, but
 * the real, current replication lag has silently drifted to 22 minutes since the plan was last
 * written (an unrelated infrastructure change increased it and nobody updated the DR plan to
 * match). Because no drill has ever actually run, this drift is never discovered -- the plan
 * looks correct on paper right up until a real disaster is the first time anyone finds out.
 */
public class Main {
    private static final int RTO_MINUTES = 15;                    // promised: recover within 15 minutes
    private static final int RPO_MINUTES = 5;                     // promised: lose no more than 5 minutes of data
    private static final int DOCUMENTED_REPLICATION_LAG_MINUTES = 2;  // what the written plan says
    private static final int ACTUAL_REPLICATION_LAG_MINUTES = 22;      // what it has actually drifted to, undocumented
    private static final int FAILOVER_STEPS_MINUTES = 20;         // DNS switch, verification, traffic cutover

    public static void main(String[] args) {
        System.out.println("Documented DR plan (RTO/RPO objectives):");
        System.out.println("  RTO objective: " + RTO_MINUTES + " minutes");
        System.out.println("  RPO objective: " + RPO_MINUTES + " minutes");
        System.out.println("  Documented replication lag: " + DOCUMENTED_REPLICATION_LAG_MINUTES + " minutes");

        System.out.println();
        System.out.println("Drill status: no failover drill has ever actually been run against this plan. "
                + "The plan was reviewed on paper and judged sufficient.");

        int hypotheticalDataLoss = ACTUAL_REPLICATION_LAG_MINUTES; // data loss on failover approximates replication lag
        int hypotheticalRecoveryTime = ACTUAL_REPLICATION_LAG_MINUTES + FAILOVER_STEPS_MINUTES;

        System.out.println();
        System.out.println("Actual current replication lag (undocumented drift since the plan was written): "
                + ACTUAL_REPLICATION_LAG_MINUTES + " minutes");
        System.out.println("If this plan were exercised today, hypothetical data loss: " + hypotheticalDataLoss
                + " minutes -- already " + (hypotheticalDataLoss - RPO_MINUTES) + " minutes past the "
                + RPO_MINUTES + "-minute RPO objective");
        System.out.println("If this plan were exercised today, hypothetical recovery time: " + hypotheticalRecoveryTime
                + " minutes -- already " + (hypotheticalRecoveryTime - RTO_MINUTES) + " minutes past the "
                + RTO_MINUTES + "-minute RTO objective");

        System.out.println();
        System.out.println("None of this was ever discovered by testing, because no drill was ever run -- "
                + "the plan's documented numbers were simply assumed to still be accurate");
    }
}
