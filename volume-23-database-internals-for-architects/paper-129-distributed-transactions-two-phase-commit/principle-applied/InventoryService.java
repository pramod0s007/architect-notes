import java.util.HashMap;
import java.util.Map;

/**
 * An inventory service that participates in two-phase commit,
 * mirroring PaymentService's prepare/commit/abort contract: prepare()
 * tentatively holds stock and votes, commit() makes it final, and
 * abort() releases the hold.
 */
public class InventoryService {
    private final Map<String, Integer> stock = new HashMap<>();
    private final Map<String, Integer> pendingHolds = new HashMap<>();

    public InventoryService(String item, int initialStock) {
        stock.put(item, initialStock);
    }

    /** Tentatively holds the stock and votes true, or votes false without changing any state. */
    public boolean prepare(String txnId, String item, int quantity) {
        int available = stock.get(item);
        if (available < quantity) {
            return false;
        }
        stock.put(item, available - quantity);
        pendingHolds.put(txnId, quantity);
        return true;
    }

    /** Makes the held reservation final. Assumes prepare() already succeeded for this txnId. */
    public void commit(String txnId) {
        pendingHolds.remove(txnId);
    }

    /** Releases the hold, restoring the stock as if prepare() had never happened. */
    public void abort(String item, String txnId) {
        Integer held = pendingHolds.remove(txnId);
        if (held != null) {
            stock.put(item, stock.get(item) + held);
        }
    }

    public int getStock(String item) {
        return stock.get(item);
    }
}
