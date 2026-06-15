/**
 * IrrefundablePayment — base contract for payment types that cannot be reversed.
 *
 * Exposes only the operations every payment processor can safely honour:
 * sending money and querying its status.  Refund is intentionally absent.
 */
public interface IrrefundablePayment {

    /**
     * Initiate a payment of the given amount to the recipient.
     *
     * @param amount    positive dollar amount
     * @param recipient account identifier or address
     */
    void process(double amount, String recipient);

    /**
     * Query the current state of a transaction.
     *
     * @param transactionId opaque ID returned at process time
     * @return human-readable status string
     */
    String getStatus(String transactionId);
}
