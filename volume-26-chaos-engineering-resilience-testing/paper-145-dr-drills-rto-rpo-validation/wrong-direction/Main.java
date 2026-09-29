/**
 * WRONG DIRECTION: after the pressure scenario's never-tested plan, the team improves the
 * practice by actually running a failover drill -- but the drill only checks the binary question
 * "did failover eventually complete," with no comparison of the measured recovery time against
 * the RTO objective and no pass/fail check of the measured data loss against the RPO objective.
 * The same real replication-lag drift as the pressure scenario is present, and failover does
 * eventually complete -- so the drill reports PASS, even though the measured recovery time and
 * measured data loss both blew past what was committed to.
 */
public class Main {
    private static final int RTO_MINUTES = 15;
    private static final int RPO_MINUTES = 5;
    private static final int ACTUAL_REPLICATION_LAG_MINUTES = 22;
    private static final int FAILOVER_STEPS_MINUTES = 20;

    public static void main(String[] args) {
        System.out.println("Documented DR plan (RTO/RPO objectives):");
        System.out.println("  RTO objective: " + RTO_MINUTES + " minutes");
        System.out.println("  RPO objective: " + RPO_MINUTES + " minutes");

        System.out.println();
        System.out.println("Drill status: a failover drill is actually run this time.");

        int actualDataLoss = ACTUAL_REPLICATION_LAG_MINUTES;
        int actualRecoveryTime = ACTUAL_REPLICATION_LAG_MINUTES + FAILOVER_STEPS_MINUTES;

        boolean failoverCompleted = true; // it does, eventually, complete -- this is the only thing checked

        System.out.println();
        System.out.println("Failover executed. Replication lag encountered: " + ACTUAL_REPLICATION_LAG_MINUTES + " minutes.");
        System.out.println("Failover steps (DNS switch, verification, traffic cutover): " + FAILOVER_STEPS_MINUTES + " minutes.");
        System.out.println("Failover completed: " + failoverCompleted);

        System.out.println();
        System.out.println("Drill result: " + (failoverCompleted ? "PASS" : "FAIL")
                + " -- failover completed successfully");
        System.out.println("(Actual recovery time was " + actualRecoveryTime + " minutes and actual data loss was "
                + actualDataLoss + " minutes, but neither was ever compared against the " + RTO_MINUTES
                + "-minute RTO or " + RPO_MINUTES + "-minute RPO objectives -- the drill only checked "
                + "whether failover eventually happened, not whether it happened fast enough or with little enough data loss)");
    }
}
