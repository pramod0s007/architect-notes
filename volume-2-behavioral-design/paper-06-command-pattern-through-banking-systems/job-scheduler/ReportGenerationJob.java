import java.time.Instant;

/**
 * Concrete job — generate a PDF/CSV report and upload to object storage.
 * Simulates a heavy computation that always succeeds (after one retry if needed).
 */
public final class ReportGenerationJob implements Job {

    private final String jobId;
    private final String reportType;
    private final String outputBucket;
    private final Instant scheduledAt;
    private final int retryCount;

    public ReportGenerationJob(String jobId, String reportType, String outputBucket, Instant scheduledAt) {
        this(jobId, reportType, outputBucket, scheduledAt, 0);
    }

    private ReportGenerationJob(String jobId, String reportType, String outputBucket,
                                 Instant scheduledAt, int retryCount) {
        this.jobId = jobId;
        this.reportType = reportType;
        this.outputBucket = outputBucket;
        this.scheduledAt = scheduledAt;
        this.retryCount = retryCount;
    }

    @Override
    public void execute() throws Exception {
        System.out.printf("  [ReportJob %s] Generating %s report... uploading to s3://%s/%s.pdf (attempt %d)%n",
                jobId, reportType, outputBucket, reportType.toLowerCase().replace(" ", "-"),
                retryCount + 1);
    }

    @Override
    public void undo() {
        System.out.printf("  [ReportJob %s] Deleting report from s3://%s (rollback)%n", jobId, outputBucket);
    }

    @Override
    public String getJobId()        { return jobId; }
    @Override
    public Instant getScheduledAt() { return scheduledAt; }
    @Override
    public int getRetryCount()      { return retryCount; }

    @Override
    public Job withIncrementedRetry() {
        return new ReportGenerationJob(jobId, reportType, outputBucket, Instant.now(), retryCount + 1);
    }

    @Override
    public String describe() {
        return String.format("ReportJob[%s] type=%s bucket=%s", jobId, reportType, outputBucket);
    }
}
