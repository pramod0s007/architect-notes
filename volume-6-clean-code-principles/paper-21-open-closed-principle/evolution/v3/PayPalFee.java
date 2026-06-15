public class PayPalFee implements FeeStrategy {

    @Override
    public double calculate(double amount) {
        return amount * 0.034 + 0.30;
    }
}
