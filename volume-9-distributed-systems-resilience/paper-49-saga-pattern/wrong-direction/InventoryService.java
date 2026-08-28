import java.util.HashMap;
import java.util.Map;

class InventoryService {
    private final Map<String, Boolean> reservations = new HashMap<>();

    boolean reserve(Order order) {
        reservations.put(order.getId(), true);
        return true;
    }

    void release(Order order) {
        reservations.put(order.getId(), false);
    }

    boolean isReserved(Order order) {
        return reservations.getOrDefault(order.getId(), false);
    }
}
