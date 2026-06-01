/**
 * Applicant must NOT appear on the fraud/sanctions blacklist.
 *
 * Usage note: compose with {@code .not()} if you need the inverse;
 * this specification passes when the customer is not blacklisted.
 */
public final class BlacklistSpecification implements Specification<Customer> {

    @Override
    public boolean isSatisfiedBy(Customer customer) {
        return !customer.isBlacklisted();
    }

    @Override
    public String toString() {
        return "isBlacklisted == false";
    }
}
