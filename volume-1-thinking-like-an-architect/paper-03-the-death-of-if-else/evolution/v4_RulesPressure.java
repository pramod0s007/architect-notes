import java.util.*;

// =============================================================================
// v4 — RULES PRESSURE: Payment Eligibility
// =============================================================================
// Domain: Payment Processing
// Pressure type: RULES — business rules multiply, combine with AND/OR,
//                and are owned by compliance/risk teams, not engineering
//
// Month 1: 1 rule — amount below daily limit
// Month 2: Country restriction added (no payments from sanctioned countries)
// Month 4: Fraud score check added (score below threshold)
// Month 6: Daily transaction limit added (count of txns per user per day)
// Month 7: Currency support check added (not all currencies accepted)
// Month 8: Merchant tier restriction added (enterprise merchants only for large amounts)
//
// SIGNAL that this is RULES pressure (not behavior, not state):
//   - Rules combine: "allow if (amount <= limit AND country NOT in blacklist)"
//   - Rules compose into policies: fraud policy, compliance policy, merchant policy
//   - The risk team writes policies in English and engineering translates them
//   - Each rule can be in/out of a policy without changing other rules
//   - Individual rules need independent testing ("does country rule work alone?")
//
// Solution: Specification Pattern — each rule is a named, testable unit
// =============================================================================

public class v4_RulesPressure {

    static class PaymentRequest {
        private final String id;
        private final double amount;
        private final String currency;
        private final String countryCode;
        private final int fraudScore;           // 0–100, higher = more suspicious
        private final int dailyTxnCount;        // how many txns user made today
        private final String merchantTier;      // "standard", "premium", "enterprise"

        public PaymentRequest(String id, double amount, String currency,
                              String countryCode, int fraudScore,
                              int dailyTxnCount, String merchantTier) {
            this.id            = id;
            this.amount        = amount;
            this.currency      = currency;
            this.countryCode   = countryCode;
            this.fraudScore    = fraudScore;
            this.dailyTxnCount = dailyTxnCount;
            this.merchantTier  = merchantTier;
        }
        public String getId()           { return id; }
        public double getAmount()       { return amount; }
        public String getCurrency()     { return currency; }
        public String getCountryCode()  { return countryCode; }
        public int getFraudScore()      { return fraudScore; }
        public int getDailyTxnCount()   { return dailyTxnCount; }
        public String getMerchantTier() { return merchantTier; }
    }

    // ---------------------------------------------------------------------------
    // Month 1 — Single rule. Correct, minimal, no pressure.
    // ---------------------------------------------------------------------------
    static class PaymentEligibilityCheckerV1 {

        private static final double AMOUNT_LIMIT = 10_000.00;

        public boolean isEligible(PaymentRequest req) {
            // Rule 1: Amount must be within daily limit
            return req.getAmount() <= AMOUNT_LIMIT;
        }
    }

    // ---------------------------------------------------------------------------
    // Month 8 — Six rules. AND/OR combinations. RULES PRESSURE.
    //
    // [!] The if-else structure is not an algorithm — it IS the policy
    // [!] Rules combine with AND (amount AND country AND fraud), creating a
    //     truth table that is impossible to reason about in if-else form
    // [!] Compliance team requirement: "enterprise merchants can exceed amount
    //     limit IF fraud score < 30 AND country is whitelisted" — this is a
    //     COMBINATION of existing rules, not a new algorithm
    // [!] Testing rule 4 alone requires constructing a PaymentRequest that
    //     also satisfies rules 1, 2, 3 — setup cost grows exponentially
    // [!] When the risk team changes the fraud threshold from 70 to 60, they
    //     change ONE rule — but in the if-else below you must search through
    //     the entire method to find and change it safely
    // ---------------------------------------------------------------------------
    static class PaymentEligibilityCheckerV2 {

        private static final double AMOUNT_LIMIT       = 10_000.00;
        private static final double ENTERPRISE_LIMIT   = 100_000.00;
        private static final int    FRAUD_THRESHOLD    = 70;
        private static final int    DAILY_TXN_LIMIT    = 50;
        private static final Set<String> BLOCKED_COUNTRIES = new HashSet<>(
            Arrays.asList("KP", "IR", "SY", "CU")  // sanctioned country codes
        );
        private static final Set<String> SUPPORTED_CURRENCIES = new HashSet<>(
            Arrays.asList("USD", "EUR", "GBP", "JPY", "AUD", "CAD", "INR")
        );

        public boolean isEligible(PaymentRequest req) {
            // Rule 1 (Month 1): Amount limit — enterprise merchants get higher cap
            double limit = "enterprise".equals(req.getMerchantTier()) ? ENTERPRISE_LIMIT : AMOUNT_LIMIT;
            if (req.getAmount() > limit) return false;

            // Rule 2 (Month 2): Country restriction — compliance team owns this list
            if (BLOCKED_COUNTRIES.contains(req.getCountryCode())) return false;

            // Rule 3 (Month 4): Fraud score — risk team owns the threshold
            if (req.getFraudScore() >= FRAUD_THRESHOLD) return false;

            // Rule 4 (Month 6): Daily transaction limit per user
            if (req.getDailyTxnCount() >= DAILY_TXN_LIMIT) return false;

            // Rule 5 (Month 7): Currency support
            if (!SUPPORTED_CURRENCIES.contains(req.getCurrency())) return false;

            // Rule 6 (Month 8): [!] A combination rule introduced by compliance team:
            // "For amounts > $5000, merchant must be premium or enterprise"
            // [!] This is Rule 1 + merchantTier — NOT a new algorithm, a new combination
            if (req.getAmount() > 5_000.00 && "standard".equals(req.getMerchantTier())) return false;

            return true;
        }
    }

    // ---------------------------------------------------------------------------
    // v3 — Specification Pattern applied
    //
    // WHAT CHANGED:
    //   - EligibilitySpec interface with isSatisfiedBy() + and()/or()/not()
    //   - 6 named specification classes, each encapsulating one rule
    //   - PaymentEligibilityCheckerV3 composes specs into a named policy
    //   - The policy reads like the compliance team's written requirements
    //
    // WHY Specification Pattern:
    //   - Rules compose: and()/or()/not() are first-class operations
    //   - Each spec is independently testable — AmountLimitSpec can be tested
    //     without constructing a fraudScore or dailyTxnCount
    //   - New rules = new Spec class + add to policy. Zero edits to existing specs.
    //   - Policies can be defined by configuration (e.g. a risk team can assemble
    //     a stricter policy for high-value merchants by composing existing specs)
    //   - The spec names document WHY the rule exists (CountryRestrictionSpec
    //     is clearly a compliance rule, not an algorithmic one)
    // ---------------------------------------------------------------------------
    interface EligibilitySpec {
        boolean isSatisfiedBy(PaymentRequest req);

        default EligibilitySpec and(EligibilitySpec other) {
            return req -> this.isSatisfiedBy(req) && other.isSatisfiedBy(req);
        }
        default EligibilitySpec or(EligibilitySpec other) {
            return req -> this.isSatisfiedBy(req) || other.isSatisfiedBy(req);
        }
        default EligibilitySpec not() {
            return req -> !this.isSatisfiedBy(req);
        }
    }

    // Rule 1: Amount must be within the configured limit
    static class AmountLimitSpec implements EligibilitySpec {
        private final double limit;
        public AmountLimitSpec(double limit) { this.limit = limit; }
        @Override
        public boolean isSatisfiedBy(PaymentRequest req) {
            return req.getAmount() <= limit;
        }
    }

    // Rule 2: Country must not be sanctioned (compliance)
    static class CountryRestrictionSpec implements EligibilitySpec {
        private final Set<String> blocked;
        public CountryRestrictionSpec(Set<String> blocked) { this.blocked = blocked; }
        @Override
        public boolean isSatisfiedBy(PaymentRequest req) {
            return !blocked.contains(req.getCountryCode());
        }
    }

    // Rule 3: Fraud score below threshold (risk team)
    static class FraudScoreSpec implements EligibilitySpec {
        private final int maxScore;
        public FraudScoreSpec(int maxScore) { this.maxScore = maxScore; }
        @Override
        public boolean isSatisfiedBy(PaymentRequest req) {
            return req.getFraudScore() < maxScore;
        }
    }

    // Rule 4: Daily transaction count within limit
    static class DailyTxnLimitSpec implements EligibilitySpec {
        private final int maxTxns;
        public DailyTxnLimitSpec(int maxTxns) { this.maxTxns = maxTxns; }
        @Override
        public boolean isSatisfiedBy(PaymentRequest req) {
            return req.getDailyTxnCount() < maxTxns;
        }
    }

    // Rule 5: Currency must be supported
    static class CurrencySupportSpec implements EligibilitySpec {
        private final Set<String> supported;
        public CurrencySupportSpec(Set<String> supported) { this.supported = supported; }
        @Override
        public boolean isSatisfiedBy(PaymentRequest req) {
            return supported.contains(req.getCurrency());
        }
    }

    // Rule 6: High-value payments require premium+ merchant tier
    static class MerchantTierSpec implements EligibilitySpec {
        private final double highValueThreshold;
        public MerchantTierSpec(double highValueThreshold) {
            this.highValueThreshold = highValueThreshold;
        }
        @Override
        public boolean isSatisfiedBy(PaymentRequest req) {
            if (req.getAmount() <= highValueThreshold) return true;
            return "premium".equals(req.getMerchantTier()) ||
                   "enterprise".equals(req.getMerchantTier());
        }
    }

    static class PaymentEligibilityCheckerV3 {

        private static final Set<String> BLOCKED_COUNTRIES = new HashSet<>(
            Arrays.asList("KP", "IR", "SY", "CU")
        );
        private static final Set<String> SUPPORTED_CURRENCIES = new HashSet<>(
            Arrays.asList("USD", "EUR", "GBP", "JPY", "AUD", "CAD", "INR")
        );

        // Policy composition reads like the compliance document
        // Standard policy: all 6 rules in AND chain
        private final EligibilitySpec standardPolicy =
            new AmountLimitSpec(10_000.00)
            .and(new CountryRestrictionSpec(BLOCKED_COUNTRIES))
            .and(new FraudScoreSpec(70))
            .and(new DailyTxnLimitSpec(50))
            .and(new CurrencySupportSpec(SUPPORTED_CURRENCIES))
            .and(new MerchantTierSpec(5_000.00));

        // Enterprise policy: higher amount limit, same other rules
        private final EligibilitySpec enterprisePolicy =
            new AmountLimitSpec(100_000.00)
            .and(new CountryRestrictionSpec(BLOCKED_COUNTRIES))
            .and(new FraudScoreSpec(70))
            .and(new DailyTxnLimitSpec(50))
            .and(new CurrencySupportSpec(SUPPORTED_CURRENCIES));
        // Note: MerchantTierSpec not needed for enterprise — they ARE enterprise

        public boolean isEligible(PaymentRequest req) {
            EligibilitySpec policy = "enterprise".equals(req.getMerchantTier())
                ? enterprisePolicy
                : standardPolicy;
            return policy.isSatisfiedBy(req);
        }

        // Individual rule testing — key benefit of Specification Pattern
        public boolean passesFraudCheck(PaymentRequest req) {
            return new FraudScoreSpec(70).isSatisfiedBy(req);
        }
        public boolean passesCountryCheck(PaymentRequest req) {
            return new CountryRestrictionSpec(BLOCKED_COUNTRIES).isSatisfiedBy(req);
        }
    }

    public static void main(String[] args) {
        PaymentRequest eligible = new PaymentRequest(
            "TXN-001", 5000.00, "USD", "US", 20, 5, "standard");
        PaymentRequest highFraud = new PaymentRequest(
            "TXN-002", 200.00, "USD", "US", 85, 5, "standard");  // fraud score too high
        PaymentRequest sanctioned = new PaymentRequest(
            "TXN-003", 100.00, "EUR", "KP", 10, 1, "standard");  // blocked country
        PaymentRequest enterpriseBig = new PaymentRequest(
            "TXN-004", 80_000.00, "USD", "DE", 15, 2, "enterprise");
        PaymentRequest standardBig = new PaymentRequest(
            "TXN-005", 80_000.00, "USD", "DE", 15, 2, "standard");  // over standard limit

        // v1
        System.out.println("=== v1 (amount rule only) ===");
        PaymentEligibilityCheckerV1 v1 = new PaymentEligibilityCheckerV1();
        System.out.println("eligible: " + v1.isEligible(eligible));          // true
        System.out.println("highFraud (v1 doesn't check): " + v1.isEligible(highFraud)); // true (rule missing)

        // v2
        System.out.println("\n=== v2 (6 rules, if-else) ===");
        PaymentEligibilityCheckerV2 v2 = new PaymentEligibilityCheckerV2();
        System.out.println("eligible:    " + v2.isEligible(eligible));      // true
        System.out.println("highFraud:   " + v2.isEligible(highFraud));     // false
        System.out.println("sanctioned:  " + v2.isEligible(sanctioned));    // false
        System.out.println("entBig:      " + v2.isEligible(enterpriseBig)); // true
        System.out.println("standardBig: " + v2.isEligible(standardBig));   // false

        // v3 — Specification Pattern
        System.out.println("\n=== v3 (Specification Pattern) ===");
        PaymentEligibilityCheckerV3 v3 = new PaymentEligibilityCheckerV3();
        System.out.println("eligible:    " + v3.isEligible(eligible));      // true
        System.out.println("highFraud:   " + v3.isEligible(highFraud));     // false
        System.out.println("sanctioned:  " + v3.isEligible(sanctioned));    // false
        System.out.println("entBig:      " + v3.isEligible(enterpriseBig)); // true
        System.out.println("standardBig: " + v3.isEligible(standardBig));   // false

        // Test individual specs in isolation — impossible with v1/v2
        System.out.println("\n=== Spec isolation tests ===");
        System.out.println("highFraud passes fraud check: " + v3.passesFraudCheck(highFraud));      // false
        System.out.println("sanctioned passes country check: " + v3.passesCountryCheck(sanctioned)); // false
        System.out.println("eligible passes fraud check: " + v3.passesFraudCheck(eligible));         // true

        // Ad-hoc policy composition — e.g. a lenient policy for testing
        System.out.println("\n=== Ad-hoc policy (fraud only) ===");
        EligibilitySpec fraudOnlyPolicy = new FraudScoreSpec(50);
        System.out.println("eligible passes fraud-only: " + fraudOnlyPolicy.isSatisfiedBy(eligible));  // true (score=20)
        System.out.println("highFraud passes fraud-only: " + fraudOnlyPolicy.isSatisfiedBy(highFraud)); // false (score=85)
    }
}
