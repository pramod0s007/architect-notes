import java.util.HashMap;
import java.util.Map;

// One service, one local transaction. If the warehouse check fails partway
// through, the whole operation rolls back -- there's no partial state to clean up.
class LocalOrderLedger {
    private final Map<String, String> status = new HashMap<>();

    boolean placeOrder(Order order, boolean warehouseHasStock) {
        status.put(order.getId(), "PENDING");
        if (!warehouseHasStock) {
            status.remove(order.getId()); // one transaction, one rollback, done
            return false;
        }
        status.put(order.getId(), "CONFIRMED");
        return true;
    }

    String statusOf(Order order) {
        return status.getOrDefault(order.getId(), "NONE");
    }
}
