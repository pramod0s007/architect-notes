// Interface extracted speculatively — only one concrete type exists.
public interface DiscountRule {
    double apply(Order order);
}
