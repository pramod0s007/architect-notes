import java.util.Map;

// Closed for modification: this class never changes when a new payment method is added.
// Open for extension: register a new FeeStrategy implementation and it works immediately.
public class FeeCalculator {

    private final Map<String, FeeStrategy> strategies;

    public FeeCalculator(Map<String, FeeStrategy> strategies) {
        this.strategies = strategies;
    }

    public double calculate(String paymentMethod, double amount) {
        FeeStrategy strategy = strategies.get(paymentMethod);
        if (strategy == null) {
            throw new IllegalArgumentException("No fee strategy registered for: " + paymentMethod);
        }
        return strategy.calculate(amount);
    }
}
