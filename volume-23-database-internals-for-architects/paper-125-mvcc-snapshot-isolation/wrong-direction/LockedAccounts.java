import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * An account store that fixes read skew by taking a single global
 * lock for the entire duration of a read transaction (not just each
 * individual read), so no write can be interleaved between a
 * transaction's reads. This is correct -- but the same lock also
 * guards every write, so a writer has to wait for the lock to become
 * free, however long the reader happens to hold it.
 */
public class LockedAccounts {
    private final Map<String, Integer> balances = new HashMap<>();
    private final ReentrantLock lock = new ReentrantLock();

    public void open(String account, int balance) {
        lock.lock();
        try {
            balances.put(account, balance);
        } finally {
            lock.unlock();
        }
    }

    /** Acquires the lock for the whole transaction -- callers must call endReadTransaction(). */
    public void beginReadTransaction() {
        lock.lock();
    }

    public int readWithinTransaction(String account) {
        return balances.get(account);
    }

    public void endReadTransaction() {
        lock.unlock();
    }

    /** A write blocks until any in-progress read transaction releases the lock. */
    public void write(String account, int balance) {
        lock.lock();
        try {
            balances.put(account, balance);
        } finally {
            lock.unlock();
        }
    }

    public int read(String account) {
        lock.lock();
        try {
            return balances.get(account);
        } finally {
            lock.unlock();
        }
    }
}
