// PUSH channel — mobile push notification with a 50-char title and 100-char body.
// Constraints enforced at format time; output is "TITLE\nBODY".

public class PushFormatter implements NotificationFormatter {

    private static final int TITLE_MAX  =  50;
    private static final int BODY_MAX   = 100;
    private static final String ELLIPSIS = "...";

    @Override
    public String format(Order order) {
        String title = "Order #" + order.getId() + " Confirmed!";
        String body  = String.format("Hi %s! Your $%.2f order is on its way. Tap to track.",
                order.getCustomerName(), order.getTotal());

        if (title.length() > TITLE_MAX) {
            title = title.substring(0, TITLE_MAX - ELLIPSIS.length()) + ELLIPSIS;
        }
        if (body.length() > BODY_MAX) {
            body = body.substring(0, BODY_MAX - ELLIPSIS.length()) + ELLIPSIS;
        }
        return "TITLE : " + title + "\nBODY  : " + body;
    }

    @Override
    public String getChannel() {
        return "PUSH";
    }
}
