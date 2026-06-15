// v1: Direct, readable. Everything inline. Works fine for a single use case.
// No abstraction needed at this scale.
public class OrderService {

    public void placeOrder(Order order) {
        if (order.getTotal() <= 0) {
            throw new IllegalArgumentException("Order total must be positive");
        }

        System.out.println("Charging customer for order " + order.getId()
                + " — total: $" + order.getTotal());

        System.out.println("Sending confirmation email to " + order.getCustomerEmail());

        System.out.println("Saving order " + order.getId() + " to storage");

        System.out.println("Order " + order.getId() + " placed successfully");
    }

    public static void main(String[] args) {
        OrderService service = new OrderService();
        Order order = new Order("ORD-001", 149.99, "alice@example.com", false);
        service.placeOrder(order);
    }
}
