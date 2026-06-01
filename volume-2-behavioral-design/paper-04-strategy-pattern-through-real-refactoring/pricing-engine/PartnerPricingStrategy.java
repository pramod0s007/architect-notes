/**
 * Concrete strategy — B2B reseller / partner discount, 15%.
 */
public final class PartnerPricingStrategy implements PricingStrategy {

    private static final double DISCOUNT = 0.15;

    @Override
    public double calculatePrice(double basePrice, int quantity) {
        return basePrice * quantity * (1 - DISCOUNT);
    }

    @Override
    public String tierName() {
        return "Partner";
    }

    @Override
    public double discountPercent() {
        return DISCOUNT * 100;
    }
}
