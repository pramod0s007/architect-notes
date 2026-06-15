// OrderService knows nothing about MySQL, SES, or Stripe.
// It depends only on the abstractions it defines. Infrastructure is injected from outside.
public class OrderService {

    private final OrderRepository repository;
    private final EmailService emailService;
    private final FraudDetectionService fraudDetection;

    public OrderService(OrderRepository repository,
                        EmailService emailService,
                        FraudDetectionService fraudDetection) {
        this.repository = repository;
        this.emailService = emailService;
        this.fraudDetection = fraudDetection;
    }

    public void placeOrder(Order order) {
        if (order.getTotal() <= 0) {
            throw new IllegalArgumentException("Order total must be positive");
        }

        if (fraudDetection.isFraudulent(order)) {
            throw new IllegalStateException("Order flagged as fraudulent: " + order.getId());
        }

        repository.save(order);
        emailService.sendConfirmation(order.getCustomerEmail(), order);

        System.out.println("Order " + order.getId() + " placed successfully");
    }
}
