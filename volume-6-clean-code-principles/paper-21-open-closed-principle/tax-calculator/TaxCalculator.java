// OCP core — open for extension, closed for modification.
// To add Brazil: create BrazilTaxRule, register it below. That's it.
// This class never changes when a new country is introduced.

import java.util.HashMap;
import java.util.Map;

public class TaxCalculator {

    private final Map<String, TaxRule> registry = new HashMap<>();

    public TaxCalculator() {
        // Register all known tax rules at startup
        register(new UsTaxRule());
        register(new UkTaxRule());
        register(new GermanyTaxRule());
        register(new IndiaTaxRule());
        // Adding Brazil: register(new BrazilTaxRule()); — zero other changes
    }

    public void register(TaxRule rule) {
        registry.put(rule.getCountryCode().toUpperCase(), rule);
        System.out.println("[TaxCalculator] Registered rule for: " + rule.getCountryCode());
    }

    public double compute(String countryCode, double amount) {
        TaxRule rule = registry.get(countryCode.toUpperCase());
        if (rule == null) {
            throw new IllegalArgumentException(
                    "No tax rule registered for country code: " + countryCode);
        }
        double tax = rule.calculate(amount);
        System.out.printf("[TaxCalculator] %s | amount=%.2f | tax=%.2f | total=%.2f%n",
                countryCode.toUpperCase(), amount, tax, amount + tax);
        return tax;
    }
}
