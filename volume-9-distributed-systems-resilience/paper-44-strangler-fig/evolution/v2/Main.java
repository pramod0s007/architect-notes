import java.util.List;

public class Main {
    public static void main(String[] args) {
        OrderFulfillmentMonolith monolith = new OrderFulfillmentMonolith();

        Order domesticOrder = new Order("ORD-2", "CUST-2", 30.00, List.of("widget"));
        Order internationalOrder = new Order("ORD-3", "INTL-7", 75.00, List.of("gadget"));

        System.out.println(monolith.placeOrder(domesticOrder));
        System.out.println(monolith.placeOrder(internationalOrder));
        System.out.println("v2: BOTH orders went through the SAME provider — the switch is global,");
        System.out.println("    not per-order. Flipping it means every customer moves at once.");
    }
}
