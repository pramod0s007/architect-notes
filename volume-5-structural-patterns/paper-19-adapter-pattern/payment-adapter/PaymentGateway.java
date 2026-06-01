/**
 * Your application's unified payment interface.
 *
 * {@link PaymentService} and all business logic depend only on this
 * abstraction. Concrete adapters wrap the real provider SDKs.
 */
public interface PaymentGateway {

    /**
     * Charge a customer for an amount in a given currency.
     *
     * @param customerId  provider-agnostic customer identifier
     * @param amount      amount in the major unit (e.g. 19.99 for $19.99)
     * @param currency    ISO 4217 currency code (e.g. "USD", "EUR")
     * @return normalised result with a transaction ID on success
     */
    ChargeResult charge(String customerId, double amount, String currency);

    /**
     * Refund a previously completed charge.
     *
     * @param chargeId  transaction ID returned by a prior {@link #charge} call
     * @param amount    amount to refund (may be partial)
     * @return normalised result with a refund ID on success
     */
    RefundResult refund(String chargeId, double amount);
}
