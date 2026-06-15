// Single source of truth for all discount logic.
// Change the formula here once — every service gets it automatically.
public class DiscountCalculator {

    public static final double LOYALTY_RATE = 0.01;
    public static final double PROMO_RATE   = 0.10;
    public static final double MAX_RATE     = 0.30;

    public double calculate(double total, int loyaltyYears, boolean hasPromoCode) {
        double rate = 0;

        if (loyaltyYears >= 1) {
            rate += LOYALTY_RATE * loyaltyYears;
        }

        if (hasPromoCode) {
            rate += PROMO_RATE;
        }

        if (rate > MAX_RATE) {
            rate = MAX_RATE;
        }

        return total - (total * rate);
    }
}
