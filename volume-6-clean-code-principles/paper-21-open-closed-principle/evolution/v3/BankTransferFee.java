public class BankTransferFee implements FeeStrategy {

    @Override
    public double calculate(double amount) {
        return amount * 0.005;
    }
}
