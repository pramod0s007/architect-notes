import java.util.HashMap;
import java.util.Map;

/**
 * CryptoPaymentFixed — on-chain token transfer, LSP-compliant.
 *
 * Implements IrrefundablePayment only.  No refund() method exists,
 * so there is nothing to violate.  Callers that need refunds simply
 * cannot place a CryptoPaymentFixed in a List<RefundablePayment> —
 * the type system enforces the rule at compile time.
 */
public class CryptoPaymentFixed implements IrrefundablePayment {

    private final Map<String, String> chain = new HashMap<>();

    @Override
    public void process(double amount, String recipient) {
        String txId = "CRY-" + System.currentTimeMillis();
        chain.put(txId, "CONFIRMED");
        System.out.printf("CryptoFixed: broadcast %.4f tokens to %s [txId=%s]%n",
                amount, recipient, txId);
    }

    @Override
    public String getStatus(String transactionId) {
        return chain.getOrDefault(transactionId, "CONFIRMED");
    }
}
