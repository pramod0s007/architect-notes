/**
 * Applicant must be at least {@code minimumAge} years old.
 */
public final class MinimumAgeSpecification implements Specification<Customer> {

    private final int minimumAge;

    public MinimumAgeSpecification(int minimumAge) {
        this.minimumAge = minimumAge;
    }

    @Override
    public boolean isSatisfiedBy(Customer customer) {
        return customer.getAge() >= minimumAge;
    }

    @Override
    public String toString() {
        return "age >= " + minimumAge;
    }
}
