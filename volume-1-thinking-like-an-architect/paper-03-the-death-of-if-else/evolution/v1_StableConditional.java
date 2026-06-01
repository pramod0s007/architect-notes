// =============================================================================
// v1 — STABLE CONDITIONAL (Month 1 → Month 12, unchanged)
// =============================================================================
// Domain: Payment Processing
//
// This if-else is CORRECT. It has not changed in 12 months.
// Refactoring this would be wrong — it adds complexity with zero benefit.
//
// A conditional is fine when:
//   1. It has 2-3 stable branches that are unlikely to grow
//   2. The condition is a business invariant, not a variation point
//   3. It has been stable for months
//
// The if-else is the SYMPTOM. The question is whether PRESSURE is building.
// Here: no pressure. Leave it alone.
// =============================================================================

public class v1_StableConditional {

    // ---------------------------------------------------------------------------
    // PaymentValidator — this class has NOT been touched in 12 months.
    // The two conditions are business invariants, not variation points.
    // ---------------------------------------------------------------------------
    static class Payment {
        private final double amount;
        private final String currency;
        private final String payerId;

        public Payment(double amount, String currency, String payerId) {
            this.amount   = amount;
            this.currency = currency;
            this.payerId  = payerId;
        }
        public double getAmount()   { return amount; }
        public String getCurrency() { return currency; }
        public String getPayerId()  { return payerId; }
    }

    static class PaymentValidator {

        // Month 1: Two checks. No pressure. No new checks added since.
        // The algorithm: "a payment is valid iff amount > 0 AND currency is specified"
        // This is a stable business rule that belongs in a single place.
        //
        // DO NOT refactor into:
        //   - A Strategy (the algorithm isn't varying)
        //   - A Specification (the rules aren't composing or changing independently)
        //   - A chain of validators (overkill for two stable checks)
        //
        // Rule of thumb: if-else with < 4 stable branches = correct as-is.
        public boolean isValid(Payment payment) {
            if (payment.getAmount() <= 0)        return false;  // stable: amount must be positive
            if (payment.getCurrency() == null)   return false;  // stable: null check invariant
            if (payment.getCurrency().isEmpty()) return false;  // stable: empty string guard
            return true;
        }
    }

    // ---------------------------------------------------------------------------
    // PaymentRouter — also stable. Two branches. Unchanged for 9 months.
    //
    // "Domestic payments go to the local rail; international go to SWIFT."
    // This is a policy decision — it has not grown and is not expected to grow.
    // Both branches exist for fundamentally different NETWORKS, not algorithms.
    // ---------------------------------------------------------------------------
    static class PaymentRouter {

        public String route(Payment payment) {
            // stable: two rails, not growing
            if ("USD".equals(payment.getCurrency())) return "domestic-rail";
            return "swift-rail";  // all other currencies → international
        }
    }

    public static void main(String[] args) {
        PaymentValidator validator = new PaymentValidator();
        PaymentRouter    router    = new PaymentRouter();

        Payment valid       = new Payment(100.00, "USD", "user-1");
        Payment zeroAmount  = new Payment(0.0,    "EUR", "user-2");
        Payment noCurrency  = new Payment(50.00,  null,  "user-3");

        System.out.println("Valid payment:     " + validator.isValid(valid));       // true
        System.out.println("Zero amount:       " + validator.isValid(zeroAmount));  // false
        System.out.println("Null currency:     " + validator.isValid(noCurrency));  // false

        System.out.println("USD route:         " + router.route(valid));            // domestic-rail
        System.out.println("EUR route:         " + router.route(zeroAmount));       // swift-rail

        // Key point: both classes work correctly and require zero changes.
        // The if-else here is not a design smell — it is correct design.
        // Pressure is the signal, not the presence of an if-else.
    }
}
