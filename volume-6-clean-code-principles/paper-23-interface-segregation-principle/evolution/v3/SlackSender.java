// Slack implements the minimal base interface only — send and status tracking.
// No scheduling, no analytics, no templates. No lies, no workarounds.
public class SlackSender implements NotificationSender {

    @Override
    public void send(String recipient, String message) {
        System.out.println("Slack message sent to " + recipient + ": " + message);
    }

    @Override
    public DeliveryStatus getDeliveryStatus(String id) {
        return new DeliveryStatus(id, "SEEN");
    }

    public static void main(String[] args) {
        NotificationSender slack = new SlackSender();
        ScheduledSender sms = new SmsSender();
        EmailSender email = new EmailSender();  // satisfies all three capability interfaces

        slack.send("#ops-channel", "Deploy complete.");
        sms.send("+1-555-0100", "Your code is 482913");
        sms.scheduleDelivery("+1-555-0200", "Reminder", "2025-12-01T09:00");
        email.send("admin@example.com", "Monthly report attached");
        email.exportAnalytics("CSV");
        email.updateTemplate("welcome-v2", "<html>Welcome!</html>");
    }
}
