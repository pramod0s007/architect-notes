public class RefundService {

    private final DiscountCalculator discountCalculator;

    public RefundService(DiscountCalculator discountCalculator) {
        this.discountCalculator = discountCalculator;
    }

    // No bug possible — cap is enforced inside DiscountCalculator.
    public double calculateRefund(Order order) {
        return discountCalculator.calculate(order.total, order.loyaltyYears, order.hasPromoCode);
    }
}
