public class CreditCardFee implements FeeStrategy {

    @Override
    public double calculate(double amount) {
        return amount * 0.029 + 0.30;
    }
}
