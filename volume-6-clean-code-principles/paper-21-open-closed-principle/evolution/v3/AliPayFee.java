public class AliPayFee implements FeeStrategy {

    @Override
    public double calculate(double amount) {
        return amount * 0.006 + 0.15;
    }
}
