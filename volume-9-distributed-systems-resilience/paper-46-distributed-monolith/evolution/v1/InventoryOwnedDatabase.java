import java.util.HashMap;
import java.util.Map;

// InventoryService's OWN store — no other service reads or writes this.
class InventoryOwnedDatabase {
    private static final Map<String, String> reservations = new HashMap<>();

    static void insert(String orderId, String status) {
        reservations.put(orderId, status);
    }

    static String statusOf(String orderId) {
        return reservations.get(orderId);
    }
}
