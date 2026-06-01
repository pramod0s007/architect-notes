import java.time.Instant;

/**
 * Run: javac *.java && java Main
 *
 * Schedules a mix of jobs — some immediate, one designed to fail on first attempt.
 * Demonstrates deferred execution and automatic retry.
 */
public final class Main {

    public static void main(String[] args) {
        JobScheduler scheduler = new JobScheduler();

        Instant now = Instant.now();

        System.out.println("=== Scheduling jobs ===");

        // Immediate jobs
        scheduler.schedule(new EmailJob(
                "EMAIL-001", "alice@example.com", "Order Confirmation #1001", now));

        scheduler.schedule(new ReportGenerationJob(
                "REPORT-001", "Monthly Sales", "acme-reports", now));

        scheduler.schedule(new DataSyncJob(
                "SYNC-001", "Salesforce CRM", "BigQuery DW", now));

        // This job ID ends with -FAIL so EmailJob simulates SMTP failure on attempt 1
        scheduler.schedule(new EmailJob(
                "EMAIL-FAIL", "bob@example.com", "Password Reset", now));

        // A future job — should not run yet
        scheduler.schedule(new ReportGenerationJob(
                "REPORT-002", "Q2 Inventory", "acme-reports",
                now.plusSeconds(3600)));

        System.out.println();
        System.out.println("=== Running due jobs ===");
        scheduler.runDue();

        scheduler.printSummary();

        System.out.println();
        System.out.println("Note: REPORT-002 is still pending — scheduled 1 hour from now.");
    }
}
