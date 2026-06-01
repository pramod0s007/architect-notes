/**
 * Applicant must have annual income at or above {@code minimumIncome}.
 */
public final class MinimumIncomeSpecification implements Specification<Customer> {

    private final double minimumIncome;

    public MinimumIncomeSpecification(double minimumIncome) {
        this.minimumIncome = minimumIncome;
    }

    @Override
    public boolean isSatisfiedBy(Customer customer) {
        return customer.getAnnualIncome() >= minimumIncome;
    }

    @Override
    public String toString() {
        return "annualIncome >= " + minimumIncome;
    }
}
