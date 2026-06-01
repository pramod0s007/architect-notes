import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Orchestrates the loan eligibility decision.
 *
 * <p>Standard path: must pass all four base rules.
 * <p>Premium override: a premium customer with high income bypasses
 *    the credit-score requirement.
 *
 * <p>Call {@link #isEligible(Customer)} for the binary decision.
 * Call {@link #getFailedRules(Customer)} for diagnostic output.
 */
public final class EligibilityService {

    // Named rules — key = human-readable label, value = specification
    private final Map<String, Specification<Customer>> rules = new LinkedHashMap<>();

    private final Specification<Customer> premiumOverride;
    private final Specification<Customer> standardEligibility;

    public EligibilityService() {

        // Individual rules wired with thresholds
        rules.put("Minimum age (21)",       new MinimumAgeSpecification(21));
        rules.put("Minimum income (30,000)", new MinimumIncomeSpecification(30_000));
        rules.put("Minimum credit (650)",   new MinimumCreditScoreSpecification(650));
        rules.put("Not blacklisted",        new BlacklistSpecification());

        // Standard eligibility = all four rules
        standardEligibility = rules.values().stream()
                .reduce(c -> true, Specification::and);

        // Premium override lets high earners skip the credit gate
        premiumOverride = new PremiumOverrideSpecification(120_000)
                .and(new MinimumAgeSpecification(21))
                .and(new BlacklistSpecification());  // blacklist still applies
    }

    /** Returns true if the customer qualifies via standard OR premium path. */
    public boolean isEligible(Customer customer) {
        return standardEligibility.isSatisfiedBy(customer)
                || premiumOverride.isSatisfiedBy(customer);
    }

    /**
     * Returns the labels of every standard rule the customer failed.
     * An empty list means the customer passed all standard rules
     * (though they may still be eligible via the premium override).
     */
    public List<String> getFailedRules(Customer customer) {
        List<String> failed = new ArrayList<>();
        for (Map.Entry<String, Specification<Customer>> entry : rules.entrySet()) {
            if (!entry.getValue().isSatisfiedBy(customer)) {
                failed.add(entry.getKey());
            }
        }
        return failed;
    }
}
