/**
 * PaymentProcessor — base contract for all payment types.
 *
 * LSP requires that any subtype can be used wherever this interface
 * is expected without callers needing to know the concrete type.
 */
public interface PaymentProcessor {

    /**
     * Initiate a payment of the given amount to the recipient.
     *
     * @param amount    positive dollar amount
     * @param recipient account identifier or address
     */
    void process(double amount, String recipient);

    /**
     * Reverse a previously completed payment.
     *
     * @param transactionId opaque ID returned at process time
     */
    void refund(String transactionId);

    /**
     * Query the current state of a transaction.
     *
     * @param transactionId opaque ID returned at process time
     * @return human-readable status string
     */
    String getStatus(String transactionId);
}
