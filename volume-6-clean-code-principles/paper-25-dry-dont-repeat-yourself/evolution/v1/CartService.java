public class CartService {

    // One place. No DRY issue yet.
    public double calculateTotal(Order order) {
        double discount = 0;
        if (order.loyaltyYears >= 1) {
            discount = order.total * 0.01 * order.loyaltyYears;
            double maxDiscount = order.total * 0.30;
            if (discount > maxDiscount) discount = maxDiscount;
        }
        return order.total - discount;
    }

    public static void main(String[] args) {
        CartService cart = new CartService();
        Order order = new Order("O1", 200.0, 5, false, "C1");
        System.out.println("Cart total: " + cart.calculateTotal(order)); // 190.0
    }
}
