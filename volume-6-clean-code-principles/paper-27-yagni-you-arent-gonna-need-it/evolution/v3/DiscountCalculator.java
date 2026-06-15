import java.util.List;

// Interface introduced when the second type arrived — designed for actual types,
// not imagined ones. No factory, no registry. Just a list of real rules.
public class DiscountCalculator {

    private static final double MAX_DISCOUNT_RATE = 0.30;

    private final List<DiscountRule> rules;

    public DiscountCalculator(List<DiscountRule> rules) {
        this.rules = rules;
    }

    public double calculate(Order order) {
        double totalDiscount = 0;
        for (DiscountRule rule : rules) {
            totalDiscount += rule.apply(order);
        }
        double maxAllowed = order.total * MAX_DISCOUNT_RATE;
        return Math.min(totalDiscount, maxAllowed);
    }
}
