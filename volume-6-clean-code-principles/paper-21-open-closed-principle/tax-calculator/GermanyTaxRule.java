// Germany Mehrwertsteuer (MwSt) / VAT: standard rate of 19%.
// Reduced rate of 7% (food, books, public transit) is out of scope for this demo.

public class GermanyTaxRule implements TaxRule {

    private static final double MWST_STANDARD_RATE = 0.19; // 19%

    @Override
    public double calculate(double amount) {
        return amount * MWST_STANDARD_RATE;
    }

    @Override
    public String getCountryCode() {
        return "DE";
    }
}
