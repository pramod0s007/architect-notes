// OCP — the stable abstraction every country-specific rule implements.
// TaxCalculator depends on this interface, never on concrete implementations.
// Adding a new country = new class that implements TaxRule — zero changes elsewhere.

public interface TaxRule {

    /**
     * Calculates the tax amount for the given pre-tax amount.
     *
     * @param amount the purchase amount before tax (e.g. 1000.00)
     * @return the tax amount (not the total — just the tax portion)
     */
    double calculate(double amount);

    /**
     * Returns the ISO 3166-1 alpha-2 country code this rule applies to.
     * Used as the registry key in TaxCalculator.
     */
    String getCountryCode();
}
