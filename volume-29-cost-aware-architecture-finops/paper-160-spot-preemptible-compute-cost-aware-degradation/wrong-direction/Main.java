import java.util.*;

/**
 * WRONG-DIRECTION SCENARIO: one blanket checkpoint interval applied to every job.
 *
 * The fix for restart-from-scratch waste looks obvious: add checkpointing so an
 * interruption only loses progress since the last checkpoint, not the whole run.
 * But a single checkpoint interval, chosen conservatively enough to protect the
 * most interruption-prone job in the fleet, gets applied uniformly to every other
 * job too -- including a job whose own history shows it is almost never
 * interrupted. That job pays real, ongoing checkpoint overhead for protection its
 * own history says it does not need, and never recovers that overhead in reduced
 * restart waste, because it was never losing meaningful time to restarts in the
 * first place.
 */
public class Main {

    static final double SPOT_RATE_PER_HOUR = 0.10;
    static final double CHECKPOINT_OVERHEAD_HOURS = 0.01; // extra compute time spent writing state at each checkpoint

    // Interval chosen conservatively enough to bound nightly-batch-etl's per-interruption loss --
    // applied here to every job, not just the one it was chosen for.
    static final double BLANKET_CHECKPOINT_INTERVAL_HOURS = 0.25; // 15 minutes

    static class Job {
        String name;
        double runtimeHours;
        double historicalInterruptionsPerRun; // this job's own average interruption count per full run, from its own history
        double noCheckpointCost; // this job's own no-checkpoint SPOT cost, from the pressure scenario, for comparison

        Job(String name, double runtimeHours, double historicalInterruptionsPerRun, double noCheckpointCost) {
            this.name = name;
            this.runtimeHours = runtimeHours;
            this.historicalInterruptionsPerRun = historicalInterruptionsPerRun;
            this.noCheckpointCost = noCheckpointCost;
        }
    }

    // Disclosed simplifying assumption: an interruption is, on average, equally likely to land
    // anywhere between two checkpoints, so the average progress lost per interruption is half
    // the checkpoint interval.
    static double checkpointedCost(Job j, double intervalHours) {
        double checkpoints = Math.ceil(j.runtimeHours / intervalHours);
        double overheadHours = checkpoints * CHECKPOINT_OVERHEAD_HOURS;
        double partialLossHours = j.historicalInterruptionsPerRun * (intervalHours / 2.0);
        double totalHours = j.runtimeHours + overheadHours + partialLossHours;
        return totalHours * SPOT_RATE_PER_HOUR;
    }

    public static void main(String[] args) {
        List<Job> jobs = new ArrayList<>();
        jobs.add(new Job("nightly-batch-etl", 10.0, 4.0, 4.10));
        jobs.add(new Job("quick-report-job", 1.0, 0.05, 0.10));

        System.out.println("-- Blanket policy: every job checkpoints every " + BLANKET_CHECKPOINT_INTERVAL_HOURS + "h, a platform-standard interval assumed here to be sized for the worst-case job --");
        double totalBlanketCost = 0.0;
        for (Job j : jobs) {
            double cost = checkpointedCost(j, BLANKET_CHECKPOINT_INTERVAL_HOURS);
            totalBlanketCost += cost;
            System.out.printf("%-20s no-checkpoint SPOT was $%.4f -> blanket-checkpointed SPOT is $%.4f%n",
                    j.name, j.noCheckpointCost, cost);
        }
        System.out.printf("Total under blanket checkpointing: $%.4f%n", totalBlanketCost);

        System.out.println();
        System.out.println("-- What the blanket interval costs quick-report-job, which its own history says rarely needs it --");
        Job quickReport = jobs.get(1);
        double blanketCost = checkpointedCost(quickReport, BLANKET_CHECKPOINT_INTERVAL_HOURS);
        double overpay = blanketCost - quickReport.noCheckpointCost;
        double overpayPercent = (overpay / quickReport.noCheckpointCost) * 100.0;
        System.out.printf("quick-report-job: $%.4f blanket-checkpointed vs $%.4f with no checkpointing -- $%.4f (%.1f%%) of added modeled cost (checkpoint overhead plus modeled residual interruption loss) for a job whose own history shows %.2f interruptions per run%n",
                blanketCost, quickReport.noCheckpointCost, overpay, overpayPercent, quickReport.historicalInterruptionsPerRun);
    }
}
