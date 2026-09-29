import java.util.HashMap;
import java.util.Map;

/**
 * A payment service with no distributed-transaction awareness: a
 * debit either succeeds outright (funds are gone, permanently, the
 * instant this call returns) or fails outright. There is no notion of
 * a tentative hold or any way to coordinate this debit with another
 * system's action.
 */
public class PaymentService {
    private final Map<String, Integer> balances = new HashMap<>();

    public PaymentService(String account, int initialBalance) {
        balances.put(account, initialBalance);
    }

    public void debit(String account, int amount) {
        int balance = balances.get(account);
        if (balance < amount) {
            throw new IllegalStateException("Insufficient funds: balance " + balance + " < " + amount);
        }
        balances.put(account, balance - amount);
    }

    public int getBalance(String account) {
        return balances.get(account);
    }
}
