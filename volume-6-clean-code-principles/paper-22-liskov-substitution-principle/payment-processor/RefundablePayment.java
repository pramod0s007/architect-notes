/**
 * RefundablePayment — ISP / LSP fix for the payment hierarchy.
 *
 * Callers that need refund capability program to this interface.
 * Callers that only need process/status use IrrefundablePayment.
 * No method ever throws UnsupportedOperationException.
 */
public interface RefundablePayment extends IrrefundablePayment {

    /**
     * Reverse a previously completed payment.
     * Implementors must honour this contract without throwing.
     *
     * @param transactionId opaque ID returned at process time
     */
    void refund(String transactionId);
}
