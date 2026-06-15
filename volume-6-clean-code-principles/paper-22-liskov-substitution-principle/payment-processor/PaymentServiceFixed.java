import java.util.List;

/**
 * PaymentServiceFixed — no instanceof, no special-casing, no surprises.
 *
 * The type system ensures refundAll() only ever receives processors that
 * genuinely support refunds.  Adding a new irrefundable processor type
 * requires zero changes here.
 */
public class PaymentServiceFixed {

    /**
     * Refund a single transaction; safe because every item in the list
     * is guaranteed by the compiler to honour the refund contract.
     */
    public void refund(RefundablePayment processor, String transactionId) {
        processor.refund(transactionId);
    }

    /**
     * Bulk-refund a batch of transactions.
     * Works for InstantPayment, ScheduledPayment, or any future
     * RefundablePayment — no type checks needed.
     */
    public void refundAll(List<RefundablePayment> processors, String transactionId) {
        for (RefundablePayment p : processors) {
            p.refund(transactionId);
        }
    }
}
