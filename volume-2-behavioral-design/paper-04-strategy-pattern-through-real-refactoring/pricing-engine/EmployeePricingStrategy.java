/**
 * Concrete strategy — internal employee benefit pricing, 40% discount.
 */
public final class EmployeePricingStrategy implements PricingStrategy {

    private static final double DISCOUNT = 0.40;

    @Override
    public double calculatePrice(double basePrice, int quantity) {
        return basePrice * quantity * (1 - DISCOUNT);
    }

    @Override
    public String tierName() {
        return "Employee";
    }

    @Override
    public double discountPercent() {
        return DISCOUNT * 100;
    }
}
