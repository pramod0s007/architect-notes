// Four files and two abstractions to do what a 5-line method did before.
public class DiscountCalculator {

    private final DiscountRuleRegistry registry;
    private final DiscountRuleFactory  factory;

    public DiscountCalculator() {
        this.factory  = new DiscountRuleFactory();
        this.registry = new DiscountRuleRegistry();
        registry.register("loyalty", factory.create("loyalty"));
    }

    public double calculate(Order order) {
        double total = 0;
        for (DiscountRule rule : registry.getAll()) {
            total += rule.apply(order);
        }
        return total;
    }
}
