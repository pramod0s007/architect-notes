public class StripeClient {

    public boolean fraudCheck(Order order) {
        // Simplified: no order is flagged as fraud in this demo
        System.out.println("Stripe: fraud check passed for order " + order.getId());
        return false;
    }
}
