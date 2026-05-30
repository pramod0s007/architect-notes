/**
 * Run: javac *.java && java Main
 */
public final class Main {

    public static void main(String[] args) {
        Notification email = NotificationFactory.create("EMAIL");
        email.send("user@example.com", "Your order shipped");

        Notification sms = NotificationFactory.create("SMS");
        sms.send("+1-555-0100", "Your order shipped");
    }
}
