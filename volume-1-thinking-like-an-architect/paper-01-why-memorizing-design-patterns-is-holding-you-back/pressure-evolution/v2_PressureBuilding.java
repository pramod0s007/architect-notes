/**
 * v2 — NotificationService: Month 6
 *
 * Product added push notifications (sprint 4).
 * WhatsApp integration added (sprint 8).
 * Slack for enterprise customers added (sprint 11).
 *
 * The method grew from 2 branches to 5.
 * Each new channel requires modifying this method.
 * Tests for all channels live in the same test class.
 * Last sprint: 2 merge conflicts when two engineers added channels simultaneously.
 *
 * LESSON: Pressure is starting to appear.
 * The smell is not the if-else. The smell is the growth rate.
 * Not refactored yet — still manageable, but the signal is there.
 */
public class NotificationService {

    public void send(String channel, String userId, String message) {

        if (channel.equals("email")) {
            System.out.println("[Email] To: " + userId + " | " + message);

        } else if (channel.equals("sms")) {
            System.out.println("[SMS] To: " + userId + " | " + message);

        } else if (channel.equals("push")) {
            // Added sprint 4 — requires FCM token lookup
            System.out.println("[Push] To: " + userId + " | " + message);

        } else if (channel.equals("whatsapp")) {
            // Added sprint 8 — requires WhatsApp Business API
            System.out.println("[WhatsApp] To: " + userId + " | " + message);

        } else if (channel.equals("slack")) {
            // Added sprint 11 — enterprise customers only
            System.out.println("[Slack] To: " + userId + " | " + message);

        } else {
            throw new IllegalArgumentException("Unknown channel: " + channel);
        }
    }

    // ---------------------------------------------------------------
    // STATE: 5 channels. Growing. 2 merge conflicts last sprint.
    //        Every new channel: modify this method + update same test file.
    //        Teams asking to add Telegram (sprint 14) and Teams (sprint 15).
    //
    // PRESSURE: Behavior Variation — the algorithm differs per channel
    //           and channels are added faster than the caller can absorb.
    //
    // VERDICT: Pressure is real. Refactoring is now justified.
    // ---------------------------------------------------------------
}
