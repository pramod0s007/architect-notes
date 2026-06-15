public class PixFee implements FeeStrategy {

    @Override
    public double calculate(double amount) {
        return amount * 0.0099;
    }
}
