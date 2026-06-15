public class UpiFee implements FeeStrategy {

    @Override
    public double calculate(double amount) {
        return amount > 1000 ? amount * 0.009 : 0.0;
    }
}
