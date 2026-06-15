// Interface introduced when the second type arrived — not before.
// Designed for actual types, not imagined ones.
public interface DiscountRule {
    double apply(Order order);
    String getName();
}
