public class CartService {

    private final DiscountCalculator discountCalculator;

    public CartService(DiscountCalculator discountCalculator) {
        this.discountCalculator = discountCalculator;
    }

    public double calculateTotal(Order order) {
        return discountCalculator.calculate(order.total, order.loyaltyYears, order.hasPromoCode);
    }
}
