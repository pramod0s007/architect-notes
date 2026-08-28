import java.util.HashMap;
import java.util.Map;

// Nothing downstream needs to know about a reservation yet -- one service,
// one local write, nothing else to keep in sync.
class InventoryService {
    private final Map<String, Boolean> reservations = new HashMap<>();

    boolean reserve(Order order) {
        reservations.put(order.getId(), true);
        return true;
    }

    boolean isReserved(Order order) {
        return reservations.getOrDefault(order.getId(), false);
    }
}
