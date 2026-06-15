public class IdealFee implements FeeStrategy {

    @Override
    public double calculate(double amount) {
        return amount < 5000 ? amount * 0.008 + 0.25 : amount * 0.006;
    }
}
