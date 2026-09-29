import java.util.*;

/**
 * PRESSURE SCENARIO: uniform spot-with-no-checkpointing for every batch job.
 *
 * Spot instances are priced far below on-demand, so a batch-compute platform
 * moves every job to spot, on the theory that the per-hour discount alone
 * guarantees savings. Without checkpointing, a spot interruption forces a job
 * to restart from the very beginning, discarding all progress made so far. For
 * a job with a historically high interruption rate, the cumulative instance-hours
 * wasted on repeated full restarts can exceed what on-demand would have cost for
 * a single, uninterrupted run.
 */
public class Main {

    static final double SPOT_RATE_PER_HOUR = 0.10;
    static final double ON_DEMAND_RATE_PER_HOUR = 0.33;

    static class Job {
        String name;
        double runtimeHours; // actual compute needed to finish
        double[] historicalAttemptHours; // this job's own no-checkpoint attempt log: each entry is
                                          // the hours consumed before that attempt was interrupted (restarting
                                          // from scratch), except the last entry, which is the hours consumed
                                          // by the attempt that finally completed without interruption

        Job(String name, double runtimeHours, double[] historicalAttemptHours) {
            this.name = name;
            this.runtimeHours = runtimeHours;
            this.historicalAttemptHours = historicalAttemptHours;
        }
    }

    static double totalHoursConsumed(Job j) {
        double sum = 0;
        for (double h : j.historicalAttemptHours) sum += h;
        return sum;
    }

    public static void main(String[] args) {
        List<Job> jobs = new ArrayList<>();
        // nightly-batch-etl: needs 10 uninterrupted hours to finish. Its own no-checkpoint
        // history shows 6 interrupted attempts before a 7th attempt finally completed.
        jobs.add(new Job("nightly-batch-etl", 10.0, new double[]{3, 6, 2, 9, 4, 7, 10}));
        // quick-report-job: needs 1 uninterrupted hour to finish. Its own no-checkpoint
        // history shows the job completing on the first attempt every time observed.
        jobs.add(new Job("quick-report-job", 1.0, new double[]{1}));

        System.out.println("-- Uniform policy: every job runs on SPOT with no checkpointing --");
        double totalSpotCost = 0.0;
        double totalOnDemandCost = 0.0;
        for (Job j : jobs) {
            double hoursConsumed = totalHoursConsumed(j);
            double spotCost = hoursConsumed * SPOT_RATE_PER_HOUR;
            double onDemandCost = j.runtimeHours * ON_DEMAND_RATE_PER_HOUR;
            totalSpotCost += spotCost;
            totalOnDemandCost += onDemandCost;
            System.out.printf("%-20s needs %.1fh, %d attempt(s) consumed %.1fh total on SPOT ($%.2f) vs $%.2f on-demand%n",
                    j.name, j.runtimeHours, j.historicalAttemptHours.length, hoursConsumed, spotCost, onDemandCost);
        }
        System.out.printf("Total: $%.2f on SPOT (no checkpointing) vs $%.2f on-demand%n", totalSpotCost, totalOnDemandCost);

        System.out.println();
        System.out.println("-- Where the uniform no-checkpoint policy costs more than on-demand --");
        Job etl = jobs.get(0);
        double etlSpotCost = totalHoursConsumed(etl) * SPOT_RATE_PER_HOUR;
        double etlOnDemandCost = etl.runtimeHours * ON_DEMAND_RATE_PER_HOUR;
        System.out.printf("nightly-batch-etl: $%.2f on SPOT (no checkpointing) vs $%.2f on-demand -- SPOT cost this job $%.2f MORE, because 6 failed attempts restarting from the beginning consumed %.1f extra instance-hours%n",
                etlSpotCost, etlOnDemandCost, etlSpotCost - etlOnDemandCost, totalHoursConsumed(etl) - etl.runtimeHours);
    }
}
