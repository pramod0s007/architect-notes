/**
 * v3 — NotificationService: Month 9 (after refactoring)
 *
 * Strategy Pattern introduced — NOT because a book said to,
 * but because the modification cost exceeded the abstraction cost:
 *   - Every new channel required modifying send() AND the test class
 *   - Merge conflicts were happening weekly
 *   - Telegram and Teams integrations were both in-flight simultaneously
 *
 * LESSON: The pattern did not cause the refactoring.
 *         The pressure caused the refactoring.
 *         The pattern is the name we give to the result.
 */

// ── Step 1: Extract the varying behavior behind an interface ──────────────

interface DeliveryChannel {
    void deliver(String userId, String message);
    String channelName();
}

// ── Step 2: Each channel is an independent, testable class ───────────────

class EmailChannel implements DeliveryChannel {
    public void deliver(String userId, String message) {
        System.out.println("[Email] To: " + userId + " | " + message);
    }
    public String channelName() { return "email"; }
}

class SmsChannel implements DeliveryChannel {
    public void deliver(String userId, String message) {
        System.out.println("[SMS] To: " + userId + " | " + message);
    }
    public String channelName() { return "sms"; }
}

class PushChannel implements DeliveryChannel {
    public void deliver(String userId, String message) {
        System.out.println("[Push] To: " + userId + " | " + message);
    }
    public String channelName() { return "push"; }
}

class WhatsAppChannel implements DeliveryChannel {
    public void deliver(String userId, String message) {
        System.out.println("[WhatsApp] To: " + userId + " | " + message);
    }
    public String channelName() { return "whatsapp"; }
}

class SlackChannel implements DeliveryChannel {
    public void deliver(String userId, String message) {
        System.out.println("[Slack] To: " + userId + " | " + message);
    }
    public String channelName() { return "slack"; }
}

// Adding Telegram (sprint 14): one new class — NotificationService untouched.
class TelegramChannel implements DeliveryChannel {
    public void deliver(String userId, String message) {
        System.out.println("[Telegram] To: " + userId + " | " + message);
    }
    public String channelName() { return "telegram"; }
}

// ── Step 3: NotificationService becomes the stable caller ────────────────

class NotificationService {

    private final DeliveryChannel channel;

    // Channel is injected — caller decides which one
    NotificationService(DeliveryChannel channel) {
        this.channel = channel;
    }

    public void send(String userId, String message) {
        // This method never changes regardless of channel count
        channel.deliver(userId, message);
    }
}

// ── Demo ─────────────────────────────────────────────────────────────────

class Main {
    public static void main(String[] args) {
        // Switch channels by injecting different implementations
        NotificationService service = new NotificationService(new EmailChannel());
        service.send("user-42", "Your order has shipped.");

        service = new NotificationService(new SlackChannel());
        service.send("user-99", "Alert: CPU usage above 90%");

        service = new NotificationService(new TelegramChannel());
        service.send("user-17", "Your verification code is 482910");
    }
}

// ---------------------------------------------------------------
// RESULT:
//   Adding a new channel (Telegram, Teams, Discord):
//     BEFORE: modify send(), modify test class, risk merge conflict
//     AFTER:  one new class implementing DeliveryChannel
//             NotificationService: unchanged
//             Existing channels: unchanged
//             Tests: isolated per channel
//
// The pattern emerged because the pressure was real.
// ---------------------------------------------------------------
