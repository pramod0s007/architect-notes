public class RefundService {

    // BUG: copied from CartService but cap was changed incorrectly.
    // Now gives less refund — customer loses money. Should be 0.30.
    public double calculateRefund(Order order) {
        double discount = 0;
        if (order.loyaltyYears >= 1) {
            discount = order.total * 0.01 * order.loyaltyYears;
            double maxDiscount = order.total * 0.25; // BUG: should be 0.30
            if (discount > maxDiscount) discount = maxDiscount;
        }
        return order.total - discount;
    }
}
