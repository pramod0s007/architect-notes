// v3: ISP applied — interfaces split by capability, no fake methods
public class Main {
    public static void main(String[] args) {
        EmailSender email = new EmailSender();
        SmsSender sms = new SmsSender();
        SlackSender slack = new SlackSender();

        System.out.println("--- Core send (all senders) ---");
        send(email, "user@example.com", "Order confirmed");
        send(sms, "+1-555-0100", "Order confirmed");
        send(slack, "#orders", "Order confirmed");

        System.out.println("\n--- Analytics (email only — ISP enforces this) ---");
        analytics(email);
        // analytics(sms);   // compile error: SmsSender does not implement AnalyticsSender
        // analytics(slack); // compile error: correct — Slack has no analytics

        System.out.println("\n--- Template preview (email only) ---");
        System.out.println(email.previewTemplate("order-confirm"));
    }

    static void send(NotificationSender s, String recipient, String msg) {
        s.send(recipient, msg);
        System.out.println("  Status: " + s.getDeliveryStatus("tx-001").toString());
    }

    static void analytics(AnalyticsSender s) {
        System.out.println("Analytics: " + s.getAnalytics());
    }
}
