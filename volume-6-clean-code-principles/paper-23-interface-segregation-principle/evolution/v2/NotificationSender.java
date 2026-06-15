// v2: Interface has grown to serve every feature request.
// Not all senders can meaningfully implement all 8 methods — ISP violation incoming.
public interface NotificationSender {
    void send(String recipient, String message);
    DeliveryStatus getDeliveryStatus(String id);
    void scheduleDelivery(String recipient, String message, String scheduledTime);
    void cancelScheduled(String scheduleId);
    String getAnalytics();
    void exportAnalytics(String format);
    void updateTemplate(String templateId, String content);
    String previewTemplate(String templateId);
}
