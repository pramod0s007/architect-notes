import java.util.HashMap;
import java.util.Map;

/**
 * Adapts {@link PayPalSDK} to the application's {@link PaymentGateway} interface.
 *
 * Responsibilities:
 *  - Translate customer ID + amount to a PayPal order
 *  - Map PayPal's orderId+captureId model to a single transactionId
 *  - Store captureId keyed by orderId so refunds can look it up
 *  - Map PayPalRefundResponse to the normalised RefundResult
 */
public class PayPalPaymentAdapter implements PaymentGateway {

    private final PayPalSDK paypal;

    // PayPal refunds target captureId, not orderId.
    // We store the mapping: our transactionId (orderId) -> captureId
    private final Map<String, String> captureIdByTransactionId = new HashMap<>();

    public PayPalPaymentAdapter(PayPalSDK paypal) {
        this.paypal = paypal;
    }

    @Override
    public ChargeResult charge(String customerId, double amount, String currency) {
        // PayPal createOrder does not take a customerId in its basic flow;
        // in production the customer is identified during the auth/approval step.
        PayPalSDK.PayPalOrderResponse orderResponse = paypal.createOrder(amount, currency);

        if ("COMPLETED".equals(orderResponse.status)) {
            // Our "transactionId" is the orderId; captureId is stored for refunds
            captureIdByTransactionId.put(orderResponse.orderId, orderResponse.captureId);
            return ChargeResult.succeeded(orderResponse.orderId);
        } else {
            return ChargeResult.failed(orderResponse.errorDetails != null
                    ? orderResponse.errorDetails : "PayPal order failed");
        }
    }

    @Override
    public RefundResult refund(String chargeId, double amount) {
        // chargeId here is the orderId we returned from charge()
        String captureId = captureIdByTransactionId.get(chargeId);
        if (captureId == null) {
            return RefundResult.failed("No PayPal capture found for transaction: " + chargeId);
        }

        PayPalSDK.PayPalRefundResponse refundResponse = paypal.refundCapture(captureId);

        if ("COMPLETED".equals(refundResponse.status)) {
            return RefundResult.succeeded(refundResponse.refundId);
        } else {
            return RefundResult.failed("PayPal refund failed for capture: " + captureId);
        }
    }
}
