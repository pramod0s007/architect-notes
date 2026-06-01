/**
 * Premium customers with income above a threshold get a fast-track override
 * that bypasses the standard credit-score gate.
 *
 * This is itself a composable specification — it can be OR'd with the
 * standard eligibility chain.
 */
public final class PremiumOverrideSpecification implements Specification<Customer> {

    private final double incomeThreshold;

    public PremiumOverrideSpecification(double incomeThreshold) {
        this.incomeThreshold = incomeThreshold;
    }

    @Override
    public boolean isSatisfiedBy(Customer customer) {
        return customer.isPremium() && customer.getAnnualIncome() >= incomeThreshold;
    }

    @Override
    public String toString() {
        return "isPremium == true AND annualIncome >= " + incomeThreshold;
    }
}
