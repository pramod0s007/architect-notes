// US sales tax: flat 8.5% on all purchases.
// State-level variations could be handled by sub-classing this rule.

public class UsTaxRule implements TaxRule {

    private static final double SALES_TAX_RATE = 0.085; // 8.5%

    @Override
    public double calculate(double amount) {
        return amount * SALES_TAX_RATE;
    }

    @Override
    public String getCountryCode() {
        return "US";
    }
}
