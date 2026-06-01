/**
 * Application service that processes payments.
 *
 * Depends exclusively on the {@link PaymentGateway} interface — it has no
 * knowledge of Stripe, PayPal, or any other provider. Swapping the provider
 * requires only a one-line change at the injection site.
 */
public class PaymentService {

    private final PaymentGateway gateway;

    public PaymentService(PaymentGateway gateway) {
        this.gateway = gateway;
    }

    /**
     * Complete a purchase: charge the customer and (if requested) refund on
     * failure simulation.
     *
     * @param customerId  customer identifier
     * @param amount      charge amount in dollars
     * @param currency    ISO 4217 currency code
     * @return the transaction ID on success
     */
    public String completePurchase(String customerId, double amount, String currency) {
        System.out.println("  [PaymentService] Charging customer=" + customerId
                + " amount=" + String.format("%.2f", amount) + " " + currency);

        ChargeResult result = gateway.charge(customerId, amount, currency);

        if (result.isSuccess()) {
            System.out.println("  [PaymentService] Charge succeeded. Transaction: "
                    + result.getTransactionId());
            return result.getTransactionId();
        } else {
            System.out.println("  [PaymentService] Charge FAILED: " + result.getErrorMessage());
            return null;
        }
    }

    /**
     * Process a full refund for a given transaction.
     */
    public void processRefund(String transactionId, double amount) {
        System.out.println("  [PaymentService] Refunding transactionId=" + transactionId
                + " amount=" + String.format("%.2f", amount));

        RefundResult result = gateway.refund(transactionId, amount);

        if (result.isSuccess()) {
            System.out.println("  [PaymentService] Refund succeeded. Refund ID: "
                    + result.getRefundId());
        } else {
            System.out.println("  [PaymentService] Refund FAILED: " + result.getErrorMessage());
        }
    }
}
