import java.util.HashMap;
import java.util.Map;

/**
 * Same as the pressure scenario's payment service, plus a refund()
 * that can itself fail -- modeling a real payment gateway that may be
 * unavailable for the reversal even though the original debit
 * succeeded moments earlier.
 */
public class PaymentService {
    private final Map<String, Integer> balances = new HashMap<>();
    private boolean refundGatewayDown = false;

    public PaymentService(String account, int initialBalance) {
        balances.put(account, initialBalance);
    }

    public void setRefundGatewayDown(boolean down) {
        this.refundGatewayDown = down;
    }

    public void debit(String account, int amount) {
        int balance = balances.get(account);
        if (balance < amount) {
            throw new IllegalStateException("Insufficient funds: balance " + balance + " < " + amount);
        }
        balances.put(account, balance - amount);
    }

    public void refund(String account, int amount) {
        if (refundGatewayDown) {
            throw new IllegalStateException("Refund failed: payment gateway unavailable");
        }
        int balance = balances.get(account);
        balances.put(account, balance + amount);
    }

    public int getBalance(String account) {
        return balances.get(account);
    }
}
