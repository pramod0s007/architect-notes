import java.util.HashMap;
import java.util.Map;

/**
 * CryptoPayment — on-chain token transfer.
 *
 * LSP violation: refund() throws an unchecked exception that no caller
 * expects from the PaymentProcessor contract.  Any code that iterates
 * a List<PaymentProcessor> and calls refund() will blow up at runtime
 * when it encounters this type — callers are forced into instanceof checks.
 */
public class CryptoPayment implements PaymentProcessor {

    private final Map<String, String> chain = new HashMap<>();

    @Override
    public void process(double amount, String recipient) {
        String txId = "CRY-" + System.currentTimeMillis();
        chain.put(txId, "CONFIRMED");
        System.out.printf("Crypto: broadcast %.4f tokens to %s [txId=%s]%n",
                amount, recipient, txId);
    }

    // LSP violation — callers must instanceof-check before calling refund()
    @Override
    public void refund(String transactionId) {
        throw new UnsupportedOperationException(
                "Blockchain transactions are irreversible");
    }

    @Override
    public String getStatus(String transactionId) {
        return chain.getOrDefault(transactionId, "CONFIRMED");
    }
}
