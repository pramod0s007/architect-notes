// OCP core — dispatches to every registered formatter without knowing their internals.
// Adding a new channel (e.g. WhatsAppFormatter): instantiate it and pass it in.
// This class never changes when new channels are added.

import java.util.ArrayList;
import java.util.List;

public class NotificationDispatcher {

    private final List<NotificationFormatter> formatters;

    public NotificationDispatcher(List<NotificationFormatter> formatters) {
        this.formatters = new ArrayList<>(formatters);
    }

    public void dispatch(Order order) {
        System.out.println("[Dispatcher] Dispatching notifications for " + order);
        System.out.println("[Dispatcher] Channels active: " + formatters.size() + "\n");

        for (NotificationFormatter formatter : formatters) {
            String channel = formatter.getChannel();
            String message = formatter.format(order);
            System.out.println("┌─ Channel: " + channel + " " + "─".repeat(Math.max(0, 38 - channel.length())));
            for (String line : message.split("\n")) {
                System.out.println("│ " + line);
            }
            System.out.println("└" + "─".repeat(40));
            System.out.println();
        }
    }
}
