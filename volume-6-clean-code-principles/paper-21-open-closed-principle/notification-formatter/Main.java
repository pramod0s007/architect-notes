import java.util.Arrays;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        System.out.println("══════════════════════════════════════════");
        System.out.println(" Notification Formatter — OCP Demo");
        System.out.println(" Open for extension, closed for modification");
        System.out.println("══════════════════════════════════════════\n");

        Order order = new Order(
                "ORD-78451",
                "Priya Mehta",
                Arrays.asList("Wireless Headphones", "USB-C Hub", "Laptop Stand"),
                247.98
        );

        List<NotificationFormatter> formatters = List.of(
                new EmailFormatter(),
                new SmsFormatter(),
                new PushFormatter()
                // Adding WhatsApp: new WhatsAppFormatter() — zero changes above
        );

        NotificationDispatcher dispatcher = new NotificationDispatcher(formatters);
        dispatcher.dispatch(order);

        System.out.println("══════════════════════════════════════════");
        System.out.println(" Second order — minimal items, high value");
        System.out.println("══════════════════════════════════════════\n");

        Order premiumOrder = new Order(
                "ORD-99002",
                "Alexander Konstantinidis",
                List.of("MacBook Pro 16-inch"),
                3299.00
        );
        dispatcher.dispatch(premiumOrder);
    }
}
