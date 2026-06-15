import java.util.HashMap;
import java.util.Map;

/**
 * InstantPaymentFixed — card / ACH debit, LSP-compliant.
 * Implements RefundablePayment: all three operations are fully supported.
 */
public class InstantPaymentFixed implements RefundablePayment {

    private final Map<String, String> ledger = new HashMap<>();

    @Override
    public void process(double amount, String recipient) {
        String txId = "INS-" + System.currentTimeMillis();
        ledger.put(txId, "COMPLETED");
        System.out.printf("InstantFixed: charged $%.2f to %s [txId=%s]%n",
                amount, recipient, txId);
    }

    @Override
    public void refund(String transactionId) {
        ledger.put(transactionId, "REFUNDED");
        System.out.println("InstantFixed: refunded transaction " + transactionId);
    }

    @Override
    public String getStatus(String transactionId) {
        return ledger.getOrDefault(transactionId, "COMPLETED");
    }
}
