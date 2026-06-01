import java.util.UUID;

/**
 * Simulated Stripe Java SDK.
 *
 * In production this would be the real stripe-java library classes.
 * The Stripe SDK speaks its own domain language (StripeCharge,
 * StripeChargeRequest, amountCents) which is incompatible with the
 * application's PaymentGateway interface.
 */
public class StripeSDK {

    // ── Stripe's own request/response types ──────────────────────────────

    public static class StripeChargeRequest {
        public final String customerId;
        public final long   amountCents;   // Stripe uses cents, not dollars
        public final String currency;

        public StripeChargeRequest(String customerId, long amountCents, String currency) {
            this.customerId  = customerId;
            this.amountCents = amountCents;
            this.currency    = currency;
        }
    }

    public static class StripeCharge {
        public final String id;
        public final String status;   // "succeeded" | "failed"
        public final String failureMessage;

        public StripeCharge(String id, String status, String failureMessage) {
            this.id             = id;
            this.status         = status;
            this.failureMessage = failureMessage;
        }
    }

    public static class StripeRefund {
        public final String id;
        public final String status;

        public StripeRefund(String id, String status) {
            this.id     = id;
            this.status = status;
        }
    }

    // ── SDK methods (Stripe-specific naming) ──────────────────────────────

    public StripeCharge createCharge(StripeChargeRequest request) {
        System.out.println("  [Stripe SDK] POST /v1/charges"
                + " customer=" + request.customerId
                + " amount=" + request.amountCents + " cents"
                + " currency=" + request.currency);
        String chargeId = "ch_" + UUID.randomUUID().toString().replace("-", "").substring(0, 14);
        return new StripeCharge(chargeId, "succeeded", null);
    }

    public StripeRefund createRefund(String chargeId, long amountCents) {
        System.out.println("  [Stripe SDK] POST /v1/refunds"
                + " charge=" + chargeId + " amount=" + amountCents + " cents");
        String refundId = "re_" + UUID.randomUUID().toString().replace("-", "").substring(0, 14);
        return new StripeRefund(refundId, "succeeded");
    }
}
