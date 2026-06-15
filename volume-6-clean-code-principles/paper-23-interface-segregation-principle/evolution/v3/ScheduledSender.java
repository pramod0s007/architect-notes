// Opt-in capability: only senders that support scheduling implement this.
public interface ScheduledSender extends NotificationSender {
    void scheduleDelivery(String recipient, String message, String scheduledTime);
    void cancelScheduled(String scheduleId);
}
