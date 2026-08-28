import java.util.HashMap;
import java.util.Map;

// ONE table, written by both InventoryService and ShippingService.
class SharedOrderDatabase {
    private static final Map<String, String> orders = new HashMap<>();

    static void execute(String column, String orderId, String value) {
        orders.put(orderId + "." + column, value);
    }

    static String read(String column, String orderId) {
        return orders.get(orderId + "." + column);
    }
}
