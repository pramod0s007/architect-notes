import java.util.HashMap;
import java.util.Map;

class InventoryService {
    private static final Map<String, String> reservations = new HashMap<>(); // its own store, not shared
    static volatile boolean simulateLoad = false;

    boolean reserve(Order order) {
        // still slow under load -- data ownership alone doesn't fix the latency,
        // it only removes the schema-coupling risk. The delay itself is unchanged.
        if (simulateLoad) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        reservations.put(order.getId(), "RESERVED");
        return true;
    }
}
