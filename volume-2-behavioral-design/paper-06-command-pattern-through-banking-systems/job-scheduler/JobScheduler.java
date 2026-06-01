import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Invoker — accepts Job objects without knowing what they do.
 *
 * Jobs are stored in a min-heap ordered by scheduledAt.
 * {@link #runDue()} executes every job whose scheduled time has passed.
 * Failed jobs are automatically retried up to {@code MAX_RETRIES} times.
 */
public final class JobScheduler {

    private static final int MAX_RETRIES = 3;

    /** Min-heap: earliest scheduledAt runs first. */
    private final PriorityQueue<Job> queue =
            new PriorityQueue<>(Comparator.comparing(Job::getScheduledAt));

    private final List<Job> completedJobs = new ArrayList<>();
    private final List<Job> failedJobs    = new ArrayList<>();

    public void schedule(Job job) {
        queue.offer(job);
        System.out.printf("  [Scheduler] Scheduled  %s  at %s%n", job.describe(), job.getScheduledAt());
    }

    /**
     * Run all jobs whose scheduledAt <= now.
     * On failure: retry up to MAX_RETRIES by re-queuing with incremented retry count
     * and an immediate scheduledAt (back-off omitted for clarity).
     */
    public void runDue() {
        Instant now = Instant.now();
        System.out.printf("%n  [Scheduler] Running jobs due by %s%n", now);

        List<Job> due = new ArrayList<>();
        while (!queue.isEmpty() && !queue.peek().getScheduledAt().isAfter(now)) {
            due.add(queue.poll());
        }

        if (due.isEmpty()) {
            System.out.println("  [Scheduler] No jobs due.");
            return;
        }

        for (Job job : due) {
            runWithRetry(job);
        }
    }

    private void runWithRetry(Job job) {
        try {
            job.execute();
            completedJobs.add(job);
        } catch (Exception e) {
            System.out.printf("  [Scheduler] FAILED %s — %s%n", job.describe(), e.getMessage());
            if (job.getRetryCount() < MAX_RETRIES) {
                Job retried = job.withIncrementedRetry();
                System.out.printf("  [Scheduler] Retrying %s (attempt %d/%d)%n",
                        retried.getJobId(), retried.getRetryCount() + 1, MAX_RETRIES + 1);
                // Re-enqueue and run immediately for demo clarity
                queue.offer(retried);
                Job next = queue.poll();
                if (next != null) runWithRetry(next);
            } else {
                System.out.printf("  [Scheduler] Giving up on %s after %d attempts%n",
                        job.getJobId(), job.getRetryCount() + 1);
                failedJobs.add(job);
            }
        }
    }

    public void printSummary() {
        System.out.println();
        System.out.println("  [Scheduler] Summary:");
        System.out.printf("    completed: %d, failed: %d, pending: %d%n",
                completedJobs.size(), failedJobs.size(), queue.size());
    }
}
