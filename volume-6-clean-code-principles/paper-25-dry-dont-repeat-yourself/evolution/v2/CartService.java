public class CartService {

    public double calculateTotal(Order order) {
        double discount = 0;
        if (order.loyaltyYears >= 1) {
            discount = order.total * 0.01 * order.loyaltyYears;
            double maxDiscount = order.total * 0.30;
            if (discount > maxDiscount) discount = maxDiscount;
        }
        return order.total - discount;
    }
}
