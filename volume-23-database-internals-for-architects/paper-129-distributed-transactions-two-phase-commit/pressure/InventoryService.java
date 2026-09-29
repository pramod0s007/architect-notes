import java.util.HashMap;
import java.util.Map;

/**
 * An inventory service with no distributed-transaction awareness: a
 * reservation either succeeds outright (stock is gone, permanently,
 * the instant this call returns) or fails outright. Like
 * PaymentService, it has no notion of a tentative hold.
 */
public class InventoryService {
    private final Map<String, Integer> stock = new HashMap<>();

    public InventoryService(String item, int initialStock) {
        stock.put(item, initialStock);
    }

    public void reserve(String item, int quantity) {
        int available = stock.get(item);
        if (available < quantity) {
            throw new IllegalStateException("Insufficient stock: available " + available + " < " + quantity);
        }
        stock.put(item, available - quantity);
    }

    public int getStock(String item) {
        return stock.get(item);
    }
}
