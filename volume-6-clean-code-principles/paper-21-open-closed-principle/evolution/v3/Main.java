import java.util.HashMap;
import java.util.Map;

public class Main {

    public static void main(String[] args) {
        Map<String, FeeStrategy> strategies = new HashMap<>();
        strategies.put("CREDIT_CARD",   new CreditCardFee());
        strategies.put("BANK_TRANSFER", new BankTransferFee());
        strategies.put("PAYPAL",        new PayPalFee());
        strategies.put("PIX",           new PixFee());
        strategies.put("UPI",           new UpiFee());
        strategies.put("ALIPAY",        new AliPayFee());
        strategies.put("IDEAL",         new IdealFee());

        FeeCalculator calculator = new FeeCalculator(strategies);

        printFee(calculator, "CREDIT_CARD",   100.00);
        printFee(calculator, "BANK_TRANSFER", 100.00);
        printFee(calculator, "PAYPAL",        100.00);
        printFee(calculator, "PIX",           100.00);
        printFee(calculator, "UPI",           500.00);   // free — under threshold
        printFee(calculator, "UPI",           1500.00);  // charged — over threshold
        printFee(calculator, "ALIPAY",        100.00);
        printFee(calculator, "IDEAL",         3000.00);  // tier 1
        printFee(calculator, "IDEAL",         6000.00);  // tier 2
    }

    private static void printFee(FeeCalculator calc, String method, double amount) {
        double fee = calc.calculate(method, amount);
        System.out.printf("%-15s amount=%7.2f  fee=%.4f%n", method, amount, fee);
    }
}
