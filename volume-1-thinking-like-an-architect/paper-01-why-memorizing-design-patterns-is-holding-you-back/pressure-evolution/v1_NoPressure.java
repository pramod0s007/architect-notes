/**
 * v1 — NotificationService: Month 1
 *
 * Two delivery channels. Code is simple, readable, correct.
 * No pattern is needed here. Introducing Strategy Pattern now
 * would be premature abstraction.
 *
 * LESSON: A small, stable conditional is NOT a problem.
 * Do not refactor what does not hurt.
 */
public class NotificationService {

    public void send(String channel, String userId, String message) {

        if (channel.equals("email")) {
            // Compose and send email via SMTP
            System.out.println("[Email] To: " + userId + " | " + message);

        } else if (channel.equals("sms")) {
            // Send SMS via Twilio
            System.out.println("[SMS] To: " + userId + " | " + message);

        } else {
            throw new IllegalArgumentException("Unknown channel: " + channel);
        }
    }

    // ---------------------------------------------------------------
    // STATE: 2 channels, stable for 3 months, zero merge conflicts.
    // VERDICT: Leave it alone.
    // ---------------------------------------------------------------
}
