import java.util.List;

public class Main {
    public static void main(String[] args) {
        OrderFulfillmentMonolith monolith = new OrderFulfillmentMonolith();
        Order order = new Order("ORD-1", "CUST-1", 49.99, List.of("widget"));

        System.out.println(monolith.placeOrder(order));
        System.out.println("v1: one payment path, one monolith. Fine — nothing to route between yet.");
    }
}
