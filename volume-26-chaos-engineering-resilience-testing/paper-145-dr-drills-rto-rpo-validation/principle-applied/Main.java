/**
 * PRINCIPLE APPLIED: same failover drill, same real replication-lag drift and same actual
 * recovery time and data loss as the wrong-direction scenario, but the drill now explicitly
 * measures both against the stated RTO and RPO objectives and reports an objective pass/fail for
 * each -- not just whether failover eventually happened. Because the comparison is actually made,
 * this drill correctly reports a failure, on the same underlying failover that the wrong-direction
 * scenario reported as a PASS.
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
        System.out.println("Drill status: a failover drill is actually run, and measured against the stated objectives.");

        int actualDataLoss = ACTUAL_REPLICATION_LAG_MINUTES;
        int actualRecoveryTime = ACTUAL_REPLICATION_LAG_MINUTES + FAILOVER_STEPS_MINUTES;

        System.out.println();
        System.out.println("Failover executed. Replication lag encountered: " + ACTUAL_REPLICATION_LAG_MINUTES + " minutes.");
        System.out.println("Failover steps (DNS switch, verification, traffic cutover): " + FAILOVER_STEPS_MINUTES + " minutes.");
        System.out.println("Measured recovery time: " + actualRecoveryTime + " minutes.");
        System.out.println("Measured data loss: " + actualDataLoss + " minutes.");

        boolean rtoMet = actualRecoveryTime <= RTO_MINUTES;
        boolean rpoMet = actualDataLoss <= RPO_MINUTES;

        System.out.println();
        System.out.println("RTO check: measured recovery time " + actualRecoveryTime + " minutes vs. " + RTO_MINUTES
                + "-minute objective -- " + (rtoMet ? "MET" : "VIOLATED by " + (actualRecoveryTime - RTO_MINUTES) + " minutes"));
        System.out.println("RPO check: measured data loss " + actualDataLoss + " minutes vs. " + RPO_MINUTES
                + "-minute objective -- " + (rpoMet ? "MET" : "VIOLATED by " + (actualDataLoss - RPO_MINUTES) + " minutes"));

        boolean drillPassed = rtoMet && rpoMet;
        System.out.println();
        System.out.println("Drill result: " + (drillPassed ? "PASS" : "FAIL")
                + " -- this failover works, eventually, but does not meet what was promised, "
                + "and the drill reports that honestly instead of only checking whether it eventually happened");
    }
}
