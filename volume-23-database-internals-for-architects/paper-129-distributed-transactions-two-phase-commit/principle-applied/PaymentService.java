import java.util.HashMap;
import java.util.Map;

/**
 * A payment service that participates in two-phase commit: prepare()
 * only tentatively holds funds (removing them from what's available,
 * but not yet a final, irreversible debit) and votes on whether it
 * can go through; the actual debit only becomes final on commit(),
 * and abort() releases the hold as if prepare() had never happened.
 */
public class PaymentService {
    private final Map<String, Integer> balances = new HashMap<>();
    private final Map<String, Integer> pendingHolds = new HashMap<>();

    public PaymentService(String account, int initialBalance) {
        balances.put(account, initialBalance);
    }

    /** Tentatively holds the funds and votes true, or votes false without changing any state. */
    public boolean prepare(String txnId, String account, int amount) {
        int balance = balances.get(account);
        if (balance < amount) {
            return false;
        }
        balances.put(account, balance - amount);
        pendingHolds.put(txnId, amount);
        return true;
    }

    /** Makes the held debit final. Assumes prepare() already succeeded for this txnId. */
    public void commit(String txnId) {
        pendingHolds.remove(txnId);
    }

    /** Releases the hold, restoring the funds as if prepare() had never happened. */
    public void abort(String account, String txnId) {
        Integer held = pendingHolds.remove(txnId);
        if (held != null) {
            balances.put(account, balances.get(account) + held);
        }
    }

    public int getBalance(String account) {
        return balances.get(account);
    }
}
