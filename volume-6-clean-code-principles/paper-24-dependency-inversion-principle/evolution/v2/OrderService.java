// DIP violation: business logic depends on infrastructure concretions.
// Cannot test without MySQL + SMTP + Stripe running.
// Swapping any provider means modifying this class.
public class OrderService {

    // Hard-coded infrastructure — no way to inject alternatives
    private final MySQLOrderRepository repository = new MySQLOrderRepository();
    private final SmtpEmailService emailService = new SmtpEmailService();
    private final StripeClient stripeClient = new StripeClient();

    public void placeOrder(Order order) {
        if (order.getTotal() <= 0) {
            throw new IllegalArgumentException("Order total must be positive");
        }

        if (stripeClient.fraudCheck(order)) {
            throw new IllegalStateException("Order flagged as fraudulent: " + order.getId());
        }

        repository.save(order);
        emailService.sendConfirmation(order.getCustomerEmail(), order);

        System.out.println("Order " + order.getId() + " placed successfully");
    }

    public static void main(String[] args) {
        OrderService service = new OrderService();
        Order order = new Order("ORD-002", 249.99, "bob@example.com", true);
        service.placeOrder(order);
    }
}
