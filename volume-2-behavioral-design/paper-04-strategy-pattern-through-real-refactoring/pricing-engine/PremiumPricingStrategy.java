/**
 * Concrete strategy — premium membership tier, 20% discount.
 */
public final class PremiumPricingStrategy implements PricingStrategy {

    private static final double DISCOUNT = 0.20;

    @Override
    public double calculatePrice(double basePrice, int quantity) {
        return basePrice * quantity * (1 - DISCOUNT);
    }

    @Override
    public String tierName() {
        return "Premium";
    }

    @Override
    public double discountPercent() {
        return DISCOUNT * 100;
    }
}
