// Email implements all three capability interfaces — every method is genuine.
public class EmailSender implements ScheduledSender, AnalyticsSender, TemplateSender {

    @Override
    public void send(String recipient, String message) {
        System.out.println("Email sent to " + recipient + ": " + message);
    }

    @Override
    public DeliveryStatus getDeliveryStatus(String id) {
        return new DeliveryStatus(id, "DELIVERED");
    }

    @Override
    public void scheduleDelivery(String recipient, String message, String scheduledTime) {
        System.out.println("Email scheduled to " + recipient + " at " + scheduledTime);
    }

    @Override
    public void cancelScheduled(String scheduleId) {
        System.out.println("Cancelled scheduled email: " + scheduleId);
    }

    @Override
    public String getAnalytics() {
        return "{\"open_rate\": 0.42, \"click_rate\": 0.18}";
    }

    @Override
    public void exportAnalytics(String format) {
        System.out.println("Exporting email analytics as " + format);
    }

    @Override
    public void updateTemplate(String templateId, String content) {
        System.out.println("Updated email template: " + templateId);
    }

    @Override
    public String previewTemplate(String templateId) {
        return "<html>Preview of template " + templateId + "</html>";
    }
}
