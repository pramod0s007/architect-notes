// UK Value Added Tax (VAT): standard rate of 20% on most goods and services.
// Reduced rate (5%) and zero rate are out of scope for this demo.

public class UkTaxRule implements TaxRule {

    private static final double VAT_STANDARD_RATE = 0.20; // 20%

    @Override
    public double calculate(double amount) {
        return amount * VAT_STANDARD_RATE;
    }

    @Override
    public String getCountryCode() {
        return "UK";
    }
}
