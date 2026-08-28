import java.util.List;

public class Main {
    public static void main(String[] args) {
        OrderFulfillmentMonolith monolith = new OrderFulfillmentMonolith();
        DirectClient client = new DirectClient(monolith);

        Order order = new Order("ORD-1", "CUST-1", 49.99, List.of("widget"));
        System.out.println(client.placeOrder(order));
        System.out.println("v1: one backend, direct call. Nothing to hide yet.");
    }
}
