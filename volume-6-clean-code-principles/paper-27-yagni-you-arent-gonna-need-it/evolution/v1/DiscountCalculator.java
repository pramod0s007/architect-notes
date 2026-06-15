// Simple, direct. One discount type. No infrastructure for imagined future types.
public class DiscountCalculator {

    public double calculate(Order order) {
        if (order.loyaltyYears >= 1) {
            return order.total * 0.10;
        }
        return 0;
    }

    public static void main(String[] args) {
        DiscountCalculator calc = new DiscountCalculator();

        Order loyal    = new Order("O1", 200.0, 3, false);
        Order newUser  = new Order("O2", 200.0, 0, false);

        System.out.println("Loyal discount:   " + calc.calculate(loyal));   // 20.0
        System.out.println("New user discount: " + calc.calculate(newUser)); // 0.0
    }
}
