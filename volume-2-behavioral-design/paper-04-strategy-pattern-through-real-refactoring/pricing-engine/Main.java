/**
 * Run: javac *.java && java Main
 *
 * Same product, four customer tiers — pricing algorithm swaps, caller stays identical.
 */
public final class Main {

    public static void main(String[] args) {
        String product = "Wireless Headphones";
        double basePrice = 120.00;

        PriceCalculator calc = new PriceCalculator(new StandardPricingStrategy());

        // Standard retail customer
        calc.printQuote(product, basePrice, 1);

        // Premium membership holder
        calc.setStrategy(new PremiumPricingStrategy());
        calc.printQuote(product, basePrice, 1);

        // Internal employee purchase (higher volume typical)
        calc.setStrategy(new EmployeePricingStrategy());
        calc.printQuote(product, basePrice, 3);

        // B2B reseller bulk order
        calc.setStrategy(new PartnerPricingStrategy());
        calc.printQuote(product, basePrice, 50);

        System.out.println();
        System.out.println("Note: PriceCalculator code never changed — only the strategy.");
    }
}
