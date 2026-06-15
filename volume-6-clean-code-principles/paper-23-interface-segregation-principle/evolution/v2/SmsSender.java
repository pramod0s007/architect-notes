// ISP violation: SMS cannot support analytics or templates.
// Forced to implement 4 methods that have no meaning for this channel.
// Every caller holding a NotificationSender reference is silently misled.
public class SmsSender implements NotificationSender {

    @Override
    public void send(String recipient, String message) {
        System.out.println("SMS sent to " + recipient + ": " + message);
    }

    @Override
    public DeliveryStatus getDeliveryStatus(String id) {
        return new DeliveryStatus(id, "SENT");
    }

    @Override
    public void scheduleDelivery(String recipient, String message, String scheduledTime) {
        System.out.println("SMS scheduled to " + recipient + " at " + scheduledTime);
    }

    @Override
    public void cancelScheduled(String scheduleId) {
        System.out.println("Cancelled scheduled SMS: " + scheduleId);
    }

    // ISP violation: SMS has no analytics capability
    @Override
    public String getAnalytics() {
        return null;
    }

    // ISP violation: SMS has no analytics to export
    @Override
    public void exportAnalytics(String format) {
        throw new UnsupportedOperationException("SMS does not support analytics export");
    }

    // ISP violation: SMS has no template system
    @Override
    public void updateTemplate(String templateId, String content) {
        // no-op — SMS has no template concept
    }

    // ISP violation: SMS has no template system
    @Override
    public String previewTemplate(String templateId) {
        return null;
    }
}
