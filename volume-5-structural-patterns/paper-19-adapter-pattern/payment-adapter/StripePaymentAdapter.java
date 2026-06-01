/**
 * Adapts {@link StripeSDK} to the application's {@link PaymentGateway} interface.
 *
 * Responsibilities:
 *  - Convert dollar amounts to cents (Stripe's unit)
 *  - Map StripeCharge fields to the normalised ChargeResult
 *  - Map StripeRefund fields to the normalised RefundResult
 *  - Convert StripeSDK exceptions to a failed result (not shown here for brevity)
 */
public class StripePaymentAdapter implements PaymentGateway {

    private final StripeSDK stripe;

    public StripePaymentAdapter(StripeSDK stripe) {
        this.stripe = stripe;
    }

    @Override
    public ChargeResult charge(String customerId, double amount, String currency) {
        // Stripe requires cents; multiply and round
        long amountCents = Math.round(amount * 100);
        StripeSDK.StripeChargeRequest request =
                new StripeSDK.StripeChargeRequest(customerId, amountCents, currency.toLowerCase());

        StripeSDK.StripeCharge stripeCharge = stripe.createCharge(request);

        if ("succeeded".equals(stripeCharge.status)) {
            return ChargeResult.succeeded(stripeCharge.id);
        } else {
            return ChargeResult.failed(stripeCharge.failureMessage != null
                    ? stripeCharge.failureMessage : "Stripe charge failed");
        }
    }

    @Override
    public RefundResult refund(String chargeId, double amount) {
        long amountCents = Math.round(amount * 100);
        StripeSDK.StripeRefund stripeRefund = stripe.createRefund(chargeId, amountCents);

        if ("succeeded".equals(stripeRefund.status)) {
            return RefundResult.succeeded(stripeRefund.id);
        } else {
            return RefundResult.failed("Stripe refund failed for charge: " + chargeId);
        }
    }
}
