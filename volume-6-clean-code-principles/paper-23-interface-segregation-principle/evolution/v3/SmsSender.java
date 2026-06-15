// SMS implements ScheduledSender only — no analytics, no templates, no fake methods.
// The type system accurately models what SMS can actually do.
public class SmsSender implements ScheduledSender {

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
}
