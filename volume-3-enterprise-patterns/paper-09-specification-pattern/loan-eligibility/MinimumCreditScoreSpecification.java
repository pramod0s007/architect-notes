/**
 * Applicant's credit score must meet or exceed {@code minimumScore}.
 */
public final class MinimumCreditScoreSpecification implements Specification<Customer> {

    private final int minimumScore;

    public MinimumCreditScoreSpecification(int minimumScore) {
        this.minimumScore = minimumScore;
    }

    @Override
    public boolean isSatisfiedBy(Customer customer) {
        return customer.getCreditScore() >= minimumScore;
    }

    @Override
    public String toString() {
        return "creditScore >= " + minimumScore;
    }
}
