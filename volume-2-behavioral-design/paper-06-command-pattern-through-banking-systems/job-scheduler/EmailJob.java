import java.time.Instant;

/**
 * Concrete job — send a transactional email.
 * Simulates occasional SMTP failure to demonstrate retry behaviour.
 */
public final class EmailJob implements Job {

    private final String jobId;
    private final String recipient;
    private final String subject;
    private final Instant scheduledAt;
    private final int retryCount;

    public EmailJob(String jobId, String recipient, String subject, Instant scheduledAt) {
        this(jobId, recipient, subject, scheduledAt, 0);
    }

    private EmailJob(String jobId, String recipient, String subject, Instant scheduledAt, int retryCount) {
        this.jobId = jobId;
        this.recipient = recipient;
        this.subject = subject;
        this.scheduledAt = scheduledAt;
        this.retryCount = retryCount;
    }

    @Override
    public void execute() throws Exception {
        // Simulate: first attempt of a specific job always fails (SMTP timeout)
        if (retryCount == 0 && jobId.endsWith("-FAIL")) {
            throw new Exception("SMTP connection timed out");
        }
        System.out.printf("  [EmailJob %s] Sent \"%s\" to %s (attempt %d)%n",
                jobId, subject, recipient, retryCount + 1);
    }

    @Override
    public void undo() {
        // Email cannot be un-sent; log a warning for audit trail
        System.out.printf("  [EmailJob %s] Undo not applicable — email cannot be recalled%n", jobId);
    }

    @Override
    public String getJobId()        { return jobId; }
    @Override
    public Instant getScheduledAt() { return scheduledAt; }
    @Override
    public int getRetryCount()      { return retryCount; }

    @Override
    public Job withIncrementedRetry() {
        return new EmailJob(jobId, recipient, subject, Instant.now(), retryCount + 1);
    }

    @Override
    public String describe() {
        return String.format("EmailJob[%s] -> %s \"%s\"", jobId, recipient, subject);
    }
}
