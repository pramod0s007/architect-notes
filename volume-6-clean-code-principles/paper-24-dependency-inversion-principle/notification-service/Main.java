/**
 * DIP — notification-service
 *
 * AlertService is wired with SMS in production and Console in tests.
 * The AlertService source code does not change between environments.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== Production: SMS channel ===");
        MessageSender smsSender = new SmsMessageSender();
        AlertService productionAlerts = new AlertService(smsSender);
        productionAlerts.sendAlert("+1-555-0100", "CRITICAL", "Database connection pool exhausted");
        productionAlerts.sendAlert("+1-555-0100", "WARNING",  "Disk usage above 80%");
        productionAlerts.sendAlert("+1-555-0100", "INFO",     "Nightly backup completed");

        System.out.println();
        System.out.println("=== Production: Slack channel ===");
        MessageSender slackSender = new SlackMessageSender("ops-alerts");
        AlertService slackAlerts = new AlertService(slackSender);
        slackAlerts.sendAlert("ops-team", "CRITICAL", "Payment service unresponsive");

        System.out.println();
        System.out.println("=== Test: Console channel (no network) ===");
        ConsoleMessageSender consoleSender = new ConsoleMessageSender();
        AlertService testAlerts = new AlertService(consoleSender);
        testAlerts.sendAlert("test-recipient", "WARNING", "Cache eviction rate elevated");

        System.out.println();
        System.out.println("Last message captured by test double: " + consoleSender.getLastMessage());
        System.out.println("AlertService source code: unchanged across all three wiring scenarios.");
    }
}
