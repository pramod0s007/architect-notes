// v1: Three payment methods. Simple, readable. No abstraction needed yet.
public class FeeCalculator {

    public double calculate(String paymentMethod, double amount) {
        switch (paymentMethod) {
            case "CREDIT_CARD":
                return amount * 0.029 + 0.30;
            case "BANK_TRANSFER":
                return amount * 0.005;
            case "PAYPAL":
                return amount * 0.034 + 0.30;
            default:
                throw new IllegalArgumentException("Unknown payment method: " + paymentMethod);
        }
    }

    public static void main(String[] args) {
        FeeCalculator calculator = new FeeCalculator();
        double amount = 100.00;

        System.out.println("Fee for CREDIT_CARD: $" + calculator.calculate("CREDIT_CARD", amount));
        System.out.println("Fee for BANK_TRANSFER: $" + calculator.calculate("BANK_TRANSFER", amount));
        System.out.println("Fee for PAYPAL: $" + calculator.calculate("PAYPAL", amount));
    }
}
