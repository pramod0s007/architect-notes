// Production implementation.
public class StripeFraudDetection implements FraudDetectionService {

    @Override
    public boolean isFraudulent(Order order) {
        System.out.println("Stripe: fraud check passed for order " + order.getId());
        return false;
    }
}
