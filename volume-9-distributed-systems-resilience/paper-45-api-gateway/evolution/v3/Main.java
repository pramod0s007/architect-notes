import java.util.List;

public class Main {
    public static void main(String[] args) {
        ApiGateway gateway = new ApiGateway();
        MobileAppClient mobile = new MobileAppClient(gateway);
        WebAppClient web = new WebAppClient(gateway);

        Order happyOrder = new Order("ORD-4", "CUST-4", 30.00, List.of("widget"));
        System.out.println("Mobile: " + mobile.placeOrder(happyOrder));
        System.out.println("Web:    " + web.placeOrder(happyOrder));

        Order decliningOrder = new Order("ORD-5", "CUST-DECLINE", 30.00, List.of("widget"));
        System.out.println("Mobile (decline): " + mobile.placeOrder(decliningOrder));
        System.out.println("Web (decline):    " + web.placeOrder(decliningOrder));
        System.out.println("v3: both clients get the IDENTICAL message for the identical failure --");
        System.out.println("    they never implemented the orchestration themselves.");
    }
}
