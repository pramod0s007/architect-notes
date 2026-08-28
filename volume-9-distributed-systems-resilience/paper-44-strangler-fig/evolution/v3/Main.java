import java.util.List;

public class Main {
    public static void main(String[] args) {
        OrderFulfillmentMonolith monolith = new OrderFulfillmentMonolith();

        Order domesticOrder = new Order("ORD-4", "CUST-4", 30.00, List.of("widget"));
        Order internationalOrder = new Order("ORD-5", "INTL-9", 75.00, List.of("gadget"));

        System.out.println(monolith.placeOrder(domesticOrder));
        System.out.println(monolith.placeOrder(internationalOrder));
        System.out.println("v3: routed per-order by the SAME PaymentRouter -- domestic stays on");
        System.out.println("    [legacy], international goes to [modern]. No redeploy, no big-bang.");
    }
}
