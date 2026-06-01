import java.util.UUID;

/**
 * Simulated PayPal REST SDK.
 *
 * PayPal's API uses a completely different model: you create an "Order"
 * (not a "Charge"), the amount is a decimal string (not cents), and a
 * refund targets a "capture" (not a "charge"). All of this must be adapted.
 */
public class PayPalSDK {

    // ── PayPal's own response types ───────────────────────────────────────

    public static class PayPalOrderResponse {
        public final String orderId;
        public final String captureId;   // the ID needed for refunds
        public final String status;      // "COMPLETED" | "FAILED"
        public final String errorDetails;

        public PayPalOrderResponse(String orderId, String captureId,
                                   String status, String errorDetails) {
            this.orderId      = orderId;
            this.captureId    = captureId;
            this.status       = status;
            this.errorDetails = errorDetails;
        }
    }

    public static class PayPalRefundResponse {
        public final String refundId;
        public final String status;  // "COMPLETED" | "FAILED"

        public PayPalRefundResponse(String refundId, String status) {
            this.refundId = refundId;
            this.status   = status;
        }
    }

    // ── SDK methods (PayPal-specific naming) ──────────────────────────────

    /**
     * Create and immediately capture a PayPal order.
     *
     * @param amount   decimal string, e.g. "19.99"
     * @param currency ISO 4217 currency code
     */
    public PayPalOrderResponse createOrder(double amount, String currency) {
        // PayPal uses payer reference via a separate auth call; simplified here
        String amountStr = String.format("%.2f", amount);
        System.out.println("  [PayPal SDK] POST /v2/checkout/orders"
                + " amount=" + amountStr + " " + currency);
        String orderId   = "PAYID-" + UUID.randomUUID().toString().substring(0, 13).toUpperCase();
        String captureId = "CAP-"   + UUID.randomUUID().toString().substring(0, 13).toUpperCase();
        return new PayPalOrderResponse(orderId, captureId, "COMPLETED", null);
    }

    /**
     * Refund a previously captured payment.
     *
     * @param captureId the capture ID from the order response
     */
    public PayPalRefundResponse refundCapture(String captureId) {
        System.out.println("  [PayPal SDK] POST /v2/payments/captures/" + captureId + "/refund");
        String refundId = "REF-" + UUID.randomUUID().toString().substring(0, 13).toUpperCase();
        return new PayPalRefundResponse(refundId, "COMPLETED");
    }
}
