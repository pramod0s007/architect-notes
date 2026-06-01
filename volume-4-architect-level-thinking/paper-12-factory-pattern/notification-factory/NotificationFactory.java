import java.util.Locale;
import java.util.Map;

public final class NotificationFactory {

    private static final Map<String, Notification> CHANNELS = Map.of(
            "EMAIL", new EmailNotification(),
            "SMS", new SmsNotification());

    private NotificationFactory() {
    }

    public static Notification create(String channel) {
        if (channel == null || channel.isBlank()) {
            throw new IllegalArgumentException("channel must not be blank");
        }
        Notification notification = CHANNELS.get(channel.toUpperCase(Locale.ROOT));
        if (notification == null) {
            throw new IllegalArgumentException("Unknown channel: " + channel);
        }
        return notification;
    }
}
