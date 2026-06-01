import java.time.Instant;

/**
 * Concrete job — synchronise data between two systems (e.g., CRM -> Data Warehouse).
 */
public final class DataSyncJob implements Job {

    private final String jobId;
    private final String sourceSystem;
    private final String targetSystem;
    private final Instant scheduledAt;
    private final int retryCount;

    public DataSyncJob(String jobId, String sourceSystem, String targetSystem, Instant scheduledAt) {
        this(jobId, sourceSystem, targetSystem, scheduledAt, 0);
    }

    private DataSyncJob(String jobId, String sourceSystem, String targetSystem,
                         Instant scheduledAt, int retryCount) {
        this.jobId = jobId;
        this.sourceSystem = sourceSystem;
        this.targetSystem = targetSystem;
        this.scheduledAt = scheduledAt;
        this.retryCount = retryCount;
    }

    @Override
    public void execute() throws Exception {
        System.out.printf("  [DataSyncJob %s] Syncing %s -> %s (attempt %d)... done, 1,204 records transferred%n",
                jobId, sourceSystem, targetSystem, retryCount + 1);
    }

    @Override
    public void undo() {
        System.out.printf("  [DataSyncJob %s] Rolling back sync: purging %s records written to %s%n",
                jobId, sourceSystem, targetSystem);
    }

    @Override
    public String getJobId()        { return jobId; }
    @Override
    public Instant getScheduledAt() { return scheduledAt; }
    @Override
    public int getRetryCount()      { return retryCount; }

    @Override
    public Job withIncrementedRetry() {
        return new DataSyncJob(jobId, sourceSystem, targetSystem, Instant.now(), retryCount + 1);
    }

    @Override
    public String describe() {
        return String.format("DataSyncJob[%s] %s -> %s", jobId, sourceSystem, targetSystem);
    }
}
