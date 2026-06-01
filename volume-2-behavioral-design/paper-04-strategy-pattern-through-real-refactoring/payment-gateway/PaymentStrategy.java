/**
 * Strategy interface — isolates payment-provider behavior variation.
 * Each provider (PayPal, Stripe, Razorpay) is a different algorithm;
 * the calling code never changes when a new provider is added.
 */
public interface PaymentStrategy {

    /** Charge the customer and return a transaction ID. */
    String pay(double amount, String currency);

    /** Issue a refund against a previous transaction. */
    void refund(String transactionId, double amount);

    /** Human-readable provider name for logging / receipts. */
    String providerName();
}
