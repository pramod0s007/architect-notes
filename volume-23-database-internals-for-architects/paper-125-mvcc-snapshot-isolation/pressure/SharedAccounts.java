import java.util.HashMap;
import java.util.Map;

/**
 * An account store with no transaction isolation at all -- reads and
 * writes go straight against a single shared, mutable map. Nothing
 * prevents a reader from observing a mix of values from before and
 * after a concurrent write.
 */
public class SharedAccounts {
    private final Map<String, Integer> balances = new HashMap<>();

    public synchronized void open(String account, int balance) {
        balances.put(account, balance);
    }

    /** Reads the current live value -- may reflect a write that happened moments ago. */
    public synchronized int read(String account) {
        return balances.get(account);
    }

    /** Applies a write directly against the shared map. */
    public synchronized void write(String account, int balance) {
        balances.put(account, balance);
    }
}
