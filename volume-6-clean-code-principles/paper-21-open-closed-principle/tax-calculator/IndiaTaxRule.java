// India Goods and Services Tax (GST):
//   Standard rate: 18% on most goods.
//   Essential goods (amount < 500 INR): concessional rate of 5%.
// Branching lives here — TaxCalculator stays untouched.

public class IndiaTaxRule implements TaxRule {

    private static final double GST_STANDARD_RATE    = 0.18; // 18%
    private static final double GST_ESSENTIALS_RATE  = 0.05; //  5%
    private static final double ESSENTIALS_THRESHOLD = 500.0;

    @Override
    public double calculate(double amount) {
        if (amount < ESSENTIALS_THRESHOLD) {
            return amount * GST_ESSENTIALS_RATE;
        }
        return amount * GST_STANDARD_RATE;
    }

    @Override
    public String getCountryCode() {
        return "IN";
    }
}
