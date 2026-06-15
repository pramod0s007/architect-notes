public class PromoCodeDiscountRule implements DiscountRule {

    private static final double PROMO_RATE = 0.15;

    @Override
    public double apply(Order order) {
        if (order.hasPromoCode) {
            return order.total * PROMO_RATE;
        }
        return 0;
    }

    @Override
    public String getName() {
        return "promo-code";
    }
}
