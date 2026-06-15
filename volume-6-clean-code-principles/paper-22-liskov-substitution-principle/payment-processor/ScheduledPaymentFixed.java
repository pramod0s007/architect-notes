import java.util.HashMap;
import java.util.Map;

/**
 * ScheduledPaymentFixed — batched wire transfer, LSP-compliant.
 * Implements RefundablePayment: all three operations are fully supported.
 */
public class ScheduledPaymentFixed implements RefundablePayment {

    private final Map<String, String> queue = new HashMap<>();

    @Override
    public void process(double amount, String recipient) {
        String txId = "SCH-" + System.currentTimeMillis();
        queue.put(txId, "PENDING");
        System.out.printf("ScheduledFixed: $%.2f to %s queued for next batch [txId=%s]%n",
                amount, recipient, txId);
    }

    @Override
    public void refund(String transactionId) {
        queue.put(transactionId, "CANCELLED");
        System.out.println("ScheduledFixed: cancelled " + transactionId + " from queue");
    }

    @Override
    public String getStatus(String transactionId) {
        return queue.getOrDefault(transactionId, "PENDING");
    }
}
