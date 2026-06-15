public class LoyaltyDiscountRule implements DiscountRule {

    private static final double RATE    = 0.10;
    private static final double MAX_CAP = 0.30;

    @Override
    public double apply(Order order) {
        if (order.loyaltyYears >= 1) {
            double rate = Math.min(RATE * order.loyaltyYears, MAX_CAP);
            return order.total * rate;
        }
        return 0;
    }

    @Override
    public String getName() {
        return "loyalty";
    }
}
