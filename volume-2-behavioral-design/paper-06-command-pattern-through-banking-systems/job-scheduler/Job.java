import java.time.Instant;

/**
 * Job — a deferrable, retryable Command.
 *
 * Extends the Command concept with scheduling metadata.
 * The scheduler treats all jobs uniformly via this interface,
 * never caring about what the job actually does.
 */
public interface Job {

    /** Execute the job's work. Throw to signal failure. */
    void execute() throws Exception;

    /** Called by the scheduler when execute() failed and a retry is needed. */
    void undo();

    /** Unique identifier — used for deduplication and logging. */
    String getJobId();

    /** When this job should first become eligible to run. */
    Instant getScheduledAt();

    /** How many times this job has already been attempted. */
    int getRetryCount();

    /** Called by the scheduler to create a new attempt with incremented retry count. */
    Job withIncrementedRetry();

    /** Short description for log output. */
    String describe();
}
