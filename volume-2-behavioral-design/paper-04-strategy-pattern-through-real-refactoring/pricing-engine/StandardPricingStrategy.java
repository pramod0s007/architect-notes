/**
 * Concrete strategy — standard retail price, no discount.
 */
public final class StandardPricingStrategy implements PricingStrategy {

    @Override
    public double calculatePrice(double basePrice, int quantity) {
        return basePrice * quantity;
    }

    @Override
    public String tierName() {
        return "Standard";
    }

    @Override
    public double discountPercent() {
        return 0.0;
    }
}
