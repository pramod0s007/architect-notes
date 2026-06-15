import java.util.HashMap;
import java.util.Map;

/**
 * ScheduledPayment — batched wire transfer that settles at end of business day.
 * Fully honours the PaymentProcessor contract; safe to substitute anywhere.
 */
public class ScheduledPayment implements PaymentProcessor {

    private final Map<String, String> queue = new HashMap<>();

    @Override
    public void process(double amount, String recipient) {
        String txId = "SCH-" + System.currentTimeMillis();
        queue.put(txId, "PENDING");
        System.out.printf("Scheduled: $%.2f to %s queued for next batch [txId=%s]%n",
                amount, recipient, txId);
    }

    @Override
    public void refund(String transactionId) {
        queue.put(transactionId, "CANCELLED");
        System.out.println("Scheduled: cancelled " + transactionId + " from queue");
    }

    @Override
    public String getStatus(String transactionId) {
        return queue.getOrDefault(transactionId, "PENDING");
    }
}
