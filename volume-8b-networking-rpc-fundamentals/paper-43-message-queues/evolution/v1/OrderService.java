/**
 * Places an order by calling the payment processor directly and
 * synchronously -- there is no queue, no retry, and no buffer between the
 * two services. If the call fails, the order is simply gone.
 */
public class OrderService {
    private final PaymentProcessor paymentProcessor;

    public OrderService(PaymentProcessor paymentProcessor) {
        this.paymentProcessor = paymentProcessor;
    }

    public void placeOrder(String orderId, double amount) {
        try {
            paymentProcessor.charge(orderId, amount);
        } catch (RuntimeException e) {
            System.out.println("FAILED to place " + orderId + ": " + e.getMessage()
                    + " -- the order is dropped, nothing retries it");
        }
    }
}
