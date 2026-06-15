import java.util.HashMap;
import java.util.Map;

/**
 * InstantPayment — card / ACH debit that settles immediately.
 * Fully honours the PaymentProcessor contract; safe to substitute anywhere.
 */
public class InstantPayment implements PaymentProcessor {

    private final Map<String, String> ledger = new HashMap<>();

    @Override
    public void process(double amount, String recipient) {
        String txId = "INS-" + System.currentTimeMillis();
        ledger.put(txId, "COMPLETED");
        System.out.printf("Instant: charged $%.2f to %s [txId=%s]%n",
                amount, recipient, txId);
    }

    @Override
    public void refund(String transactionId) {
        ledger.put(transactionId, "REFUNDED");
        System.out.println("Instant: refunded transaction " + transactionId);
    }

    @Override
    public String getStatus(String transactionId) {
        return ledger.getOrDefault(transactionId, "COMPLETED");
    }
}
