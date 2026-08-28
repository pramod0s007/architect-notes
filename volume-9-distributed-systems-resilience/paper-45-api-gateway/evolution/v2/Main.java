import java.util.List;

public class Main {
    public static void main(String[] args) {
        Order happyOrder = new Order("ORD-2", "CUST-2", 30.00, List.of("widget"));
        MobileAppClient mobile = new MobileAppClient();
        WebAppClient web = new WebAppClient();

        System.out.println("Mobile (happy path): " + mobile.placeOrder(happyOrder));
        System.out.println("Web (happy path):    " + web.placeOrder(happyOrder));

        System.out.println();
        System.out.println("Same payment failure, two independently-written clients:");
        Order decliningOrder = new Order("ORD-3", "CUST-DECLINE", 30.00, List.of("widget"));
        System.out.println("Mobile says: " + new MobileAppClient().placeOrder(decliningOrder));
        System.out.println("Web says:    " + new WebAppClient().placeOrder(decliningOrder));
        System.out.println("v2: identical failure, two different messages -- each client");
        System.out.println("    independently wired up the same call sequence.");
    }
}
