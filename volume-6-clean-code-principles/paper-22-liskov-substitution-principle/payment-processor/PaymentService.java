import java.util.List;

/**
 * PaymentService — demonstrates the instanceof sprawl that results from
 * the LSP violation in CryptoPayment.
 *
 * Every new non-refundable processor forces another branch here.
 * This is the "smell" that tells us the hierarchy is broken.
 */
public class PaymentService {

    public void processRefund(PaymentProcessor processor, String transactionId) {
        // instanceof guard — direct consequence of the LSP violation
        if (processor instanceof CryptoPayment) {
            System.out.println("[PaymentService] Cannot refund crypto transaction "
                    + transactionId + ": blockchain is irreversible");
            return;
        }
        // Every new non-refundable type needs another branch here
        processor.refund(transactionId);
    }

    /** Attempt to refund a whole batch — fails silently for crypto. */
    public void refundAll(List<PaymentProcessor> processors, String transactionId) {
        for (PaymentProcessor p : processors) {
            processRefund(p, transactionId);
        }
    }
}
