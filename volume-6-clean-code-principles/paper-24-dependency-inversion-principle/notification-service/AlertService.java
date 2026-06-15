/**
 * High-level policy: formats and dispatches alerts.
 *
 * DIP in action: depends on the MessageSender abstraction.
 * This class never changes when the delivery channel changes.
 */
public class AlertService {

    private final MessageSender sender;

    public AlertService(MessageSender sender) {
        this.sender = sender;
    }

    public void sendAlert(String recipient, String severity, String message) {
        String formatted = buildMessage(severity, message);
        sender.send(recipient, formatted);
        System.out.println("  [channel=" + sender.getChannelName() + "]");
    }

    private String buildMessage(String severity, String message) {
        switch (severity.toUpperCase()) {
            case "CRITICAL": return "🚨 CRITICAL ALERT: " + message;
            case "WARNING":  return "⚠ WARNING: " + message;
            case "INFO":     return "ℹ INFO: " + message;
            default:         return "[" + severity + "] " + message;
        }
    }
}
