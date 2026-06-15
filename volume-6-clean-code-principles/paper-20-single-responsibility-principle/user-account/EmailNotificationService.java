// AFTER SRP — owned exclusively by the Marketing team.
// Single responsibility: compose and dispatch transactional emails.
// Email copy, branding, and templates change here only.

public class EmailNotificationService {

    private static final String BANK_URL  = "https://securebank.com";
    private static final String SUPPORT   = "support@securebank.com";

    public void sendWelcomeEmail(String userId, String name) {
        String subject = "Welcome to SecureBank, " + name + "!";
        String body = String.join("\n",
                "Hi " + name + ",",
                "",
                "Your SecureBank account is ready.",
                "Log in and explore your dashboard: " + BANK_URL + "/login",
                "",
                "What you can do today:",
                "  • View your account balance",
                "  • Set up a savings goal",
                "  • Enable two-factor authentication (recommended)",
                "",
                "Questions? Reach us at " + SUPPORT,
                "",
                "— The SecureBank Team"
        );
        dispatch(userId, subject, body);
    }

    public void sendPasswordResetEmail(String userId, String name, String resetToken) {
        String subject = "SecureBank — Password Reset Request";
        String resetUrl = BANK_URL + "/reset?token=" + resetToken;
        String body = String.join("\n",
                "Hi " + name + ",",
                "",
                "We received a request to reset your password.",
                "Click the link below (valid for 30 minutes):",
                "",
                "  " + resetUrl,
                "",
                "If you did not request this, please contact " + SUPPORT + " immediately.",
                "",
                "— The SecureBank Security Team"
        );
        dispatch(userId, subject, body);
    }

    private void dispatch(String userId, String subject, String body) {
        System.out.println("[EmailService] ── Email to userId=" + userId + " ──");
        System.out.println("[EmailService] Subject : " + subject);
        System.out.println("[EmailService] Body    :");
        for (String line : body.split("\n")) {
            System.out.println("              " + line);
        }
        System.out.println("[EmailService] ── Sent ──");
    }
}
