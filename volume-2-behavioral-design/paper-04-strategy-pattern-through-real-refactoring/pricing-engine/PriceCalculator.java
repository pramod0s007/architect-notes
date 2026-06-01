/**
 * Stable context — holds and delegates to a {@link PricingStrategy}.
 * The checkout service calls this class; it never changes when tiers change.
 */
public final class PriceCalculator {

    private PricingStrategy strategy;

    public PriceCalculator(PricingStrategy strategy) {
        if (strategy == null) {
            throw new IllegalArgumentException("strategy must not be null");
        }
        this.strategy = strategy;
    }

    /** Swap tier when a customer's membership status changes mid-session. */
    public void setStrategy(PricingStrategy strategy) {
        if (strategy == null) {
            throw new IllegalArgumentException("strategy must not be null");
        }
        this.strategy = strategy;
    }

    public double calculate(double basePrice, int quantity) {
        return strategy.calculatePrice(basePrice, quantity);
    }

    public void printQuote(String productName, double basePrice, int quantity) {
        double total = calculate(basePrice, quantity);
        System.out.printf("[%-10s] %-22s qty=%-3d  base=%.2f  discount=%4.0f%%  total=%.2f%n",
                strategy.tierName(), productName, quantity,
                basePrice, strategy.discountPercent(), total);
    }
}
