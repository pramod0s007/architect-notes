// SMS channel — 160-character max constraint enforced at format time.
// Concise confirmation with order ID, total, and delivery estimate.

public class SmsFormatter implements NotificationFormatter {

    private static final int SMS_MAX_LENGTH = 160;

    @Override
    public String format(Order order) {
        String message = String.format(
                "Order #%s confirmed. Total: $%.2f. Est delivery: 3-5 days. " +
                "Track: secureshop.com/orders/%s",
                order.getId(), order.getTotal(), order.getId());

        if (message.length() > SMS_MAX_LENGTH) {
            message = message.substring(0, SMS_MAX_LENGTH - 3) + "...";
        }
        return message;
    }

    @Override
    public String getChannel() {
        return "SMS";
    }
}
