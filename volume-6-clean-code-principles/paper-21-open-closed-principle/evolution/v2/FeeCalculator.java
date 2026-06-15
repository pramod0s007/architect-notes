// v2: 7 payment methods. The switch has grown.
// Every new method modifies this class — puts existing methods at risk.
// One typo in the CREDIT_CARD branch breaks payments for all users.
public class FeeCalculator {

    public double calculate(String paymentMethod, double amount) {
        switch (paymentMethod) {
            case "CREDIT_CARD":
                return amount * 0.029 + 0.30;

            case "BANK_TRANSFER":
                return amount * 0.005;

            case "PAYPAL":
                return amount * 0.034 + 0.30;

            case "PIX":
                return amount * 0.0099;

            case "UPI":
                // Conditional pricing: free under threshold, discounted above
                return amount > 1000 ? amount * 0.009 : 0.0;

            case "ALIPAY":
                return amount * 0.006 + 0.15;

            case "IDEAL":
                // Tiered pricing: two different rates based on amount
                return amount < 5000 ? amount * 0.008 + 0.25 : amount * 0.006;

            default:
                throw new IllegalArgumentException("Unknown payment method: " + paymentMethod);
        }
    }

    public static void main(String[] args) {
        FeeCalculator calculator = new FeeCalculator();

        System.out.println("CREDIT_CARD $100:  $" + calculator.calculate("CREDIT_CARD", 100.00));
        System.out.println("PIX $100:          $" + calculator.calculate("PIX", 100.00));
        System.out.println("UPI $500:          $" + calculator.calculate("UPI", 500.00));
        System.out.println("UPI $1500:         $" + calculator.calculate("UPI", 1500.00));
        System.out.println("IDEAL $3000:       $" + calculator.calculate("IDEAL", 3000.00));
        System.out.println("IDEAL $6000:       $" + calculator.calculate("IDEAL", 6000.00));
    }
}
