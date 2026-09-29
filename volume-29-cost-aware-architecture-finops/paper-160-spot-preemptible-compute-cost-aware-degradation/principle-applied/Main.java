import java.util.*;

/**
 * PRINCIPLE-APPLIED SCENARIO: per-job checkpoint-interval selection, from each
 * job's own observed interruption history.
 *
 * Rather than checkpointing every job at one blanket interval, this policy
 * evaluates a small set of candidate checkpoint intervals (including "no
 * checkpointing at all") for each job individually, using that job's own
 * historical interruption rate, selects whichever candidate minimizes that
 * job's own projected SPOT total cost, and then compares that lowest-modeled-
 * cost SPOT candidate against this job's own on-demand cost, keeping whichever
 * of the two is cheaper. A job with too few historical runs to trust an
 * interruption-rate estimate (fewer than MIN_HISTORICAL_RUNS) is not spot-
 * scheduled off that thin sample at all -- it runs on-demand until its raw
 * run-count reaches the disclosed threshold.
 */
public class Main {

    static final double SPOT_RATE_PER_HOUR = 0.10;
    static final double ON_DEMAND_RATE_PER_HOUR = 0.33;
    static final double CHECKPOINT_OVERHEAD_HOURS = 0.01;
    static final int MIN_HISTORICAL_RUNS = 5;

    // Candidate checkpoint intervals evaluated per job. Double.POSITIVE_INFINITY represents
    // "no checkpointing" -- no checkpoint overhead, but no bound on progress lost per interruption
    // either, so it is evaluated using this job's own no-checkpoint restart-waste history instead
    // of the interval-based formula.
    static final double[] CANDIDATE_INTERVALS_HOURS = {Double.POSITIVE_INFINITY, 0.5, 0.25, 0.0833};

    static class Job {
        String name;
        double runtimeHours;
        double historicalInterruptionsPerRun;
        double assumedNoCheckpointRestartWasteHoursPerRun; // this job's assumed expected extra instance-hours wasted to full restarts on a single
                                                    // no-checkpoint run -- for nightly-batch-etl this demonstration reuses, as that per-run
                                                    // estimate, the specific above-average 7-attempt episode shown in the pressure scenario,
                                                    // rather than an average computed across many observed no-checkpoint completions; it is
                                                    // tracked independently of historicalInterruptionsPerRun below, not derived from it, so
                                                    // the two need not imply a single consistent underlying interruption rate for a given job
        int historicalRunCount;

        Job(String name, double runtimeHours, double historicalInterruptionsPerRun,
            double assumedNoCheckpointRestartWasteHoursPerRun, int historicalRunCount) {
            this.name = name;
            this.runtimeHours = runtimeHours;
            this.historicalInterruptionsPerRun = historicalInterruptionsPerRun;
            this.assumedNoCheckpointRestartWasteHoursPerRun = assumedNoCheckpointRestartWasteHoursPerRun;
            this.historicalRunCount = historicalRunCount;
        }
    }

    static double spotCostAtInterval(Job j, double intervalHours) {
        double totalHours;
        if (Double.isInfinite(intervalHours)) {
            totalHours = j.runtimeHours + j.assumedNoCheckpointRestartWasteHoursPerRun;
        } else {
            double checkpoints = Math.ceil(j.runtimeHours / intervalHours);
            double overheadHours = checkpoints * CHECKPOINT_OVERHEAD_HOURS;
            double partialLossHours = j.historicalInterruptionsPerRun * (intervalHours / 2.0);
            totalHours = j.runtimeHours + overheadHours + partialLossHours;
        }
        return totalHours * SPOT_RATE_PER_HOUR;
    }

    static double bestSpotCost(Job j) {
        double best = Double.MAX_VALUE;
        for (double interval : CANDIDATE_INTERVALS_HOURS) {
            best = Math.min(best, spotCostAtInterval(j, interval));
        }
        return best;
    }

    static double bestIntervalHours(Job j) {
        double best = Double.MAX_VALUE;
        double bestInterval = CANDIDATE_INTERVALS_HOURS[0];
        for (double interval : CANDIDATE_INTERVALS_HOURS) {
            double cost = spotCostAtInterval(j, interval);
            if (cost < best) {
                best = cost;
                bestInterval = interval;
            }
        }
        return bestInterval;
    }

    public static void main(String[] args) {
        List<Job> jobs = new ArrayList<>();
        jobs.add(new Job("nightly-batch-etl", 10.0, 4.0, 31.0, 40));
        jobs.add(new Job("quick-report-job", 1.0, 0.05, 0.0, 60));
        // new-ml-training-job: only 2 historical runs so far -- not enough to trust an
        // interruption-rate estimate, even though neither of those 2 runs happened to be interrupted.
        jobs.add(new Job("new-ml-training-job", 6.0, 0.0, 0.0, 2));

        System.out.println("-- Per-job best checkpoint interval, chosen from each job's own history --");
        double totalCost = 0.0;
        for (Job j : jobs) {
            if (j.historicalRunCount < MIN_HISTORICAL_RUNS) {
                double onDemandCost = j.runtimeHours * ON_DEMAND_RATE_PER_HOUR;
                totalCost += onDemandCost;
                System.out.printf("%-20s INSUFFICIENT_HISTORY (need >= %d runs, have %d) -- held on ON-DEMAND, $%.2f%n",
                        j.name, MIN_HISTORICAL_RUNS, j.historicalRunCount, onDemandCost);
                continue;
            }
            double bestSpot = bestSpotCost(j);
            double interval = bestIntervalHours(j);
            double onDemandCost = j.runtimeHours * ON_DEMAND_RATE_PER_HOUR;
            boolean spotWins = bestSpot <= onDemandCost;
            double chosen = spotWins ? bestSpot : onDemandCost;
            totalCost += chosen;
            if (spotWins) {
                String intervalLabel = Double.isInfinite(interval) ? "no checkpointing" : String.format("%.4fh interval", interval);
                System.out.printf("%-20s best policy: %s on SPOT, $%.4f (vs $%.2f on-demand) (history: %d runs, %.2f interruptions/run)%n",
                        j.name, intervalLabel, chosen, onDemandCost, j.historicalRunCount, j.historicalInterruptionsPerRun);
            } else {
                System.out.printf("%-20s best policy: ON-DEMAND, $%.2f (cheapest evaluated SPOT candidate was $%.4f) (history: %d runs, %.2f interruptions/run)%n",
                        j.name, chosen, bestSpot, j.historicalRunCount, j.historicalInterruptionsPerRun);
            }
        }
        System.out.printf("Total under per-job checkpoint selection: $%.4f%n", totalCost);

        System.out.println();
        System.out.println("-- What the thin-history guard is protecting against --");
        Job newJob = jobs.get(2);
        double naiveSpotCost = bestSpotCost(newJob);
        double guardedCost = newJob.runtimeHours * ON_DEMAND_RATE_PER_HOUR;
        System.out.printf("new-ml-training-job: projecting off its 2 historical runs (0 interruptions observed so far) would pick SPOT at $%.4f -- the guard instead holds it on ON-DEMAND at $%.2f until its raw run-count reaches the disclosed threshold of %d%n",
                naiveSpotCost, guardedCost, MIN_HISTORICAL_RUNS);
    }
}
