// The only real rule. Wrapped in an interface and registry for no current benefit.
public class LoyaltyDiscountRule implements DiscountRule {

    @Override
    public double apply(Order order) {
        if (order.loyaltyYears >= 1) {
            return order.total * 0.10;
        }
        return 0;
    }
}
