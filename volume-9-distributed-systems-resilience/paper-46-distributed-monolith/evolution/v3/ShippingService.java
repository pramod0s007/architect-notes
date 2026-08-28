import java.util.HashMap;
import java.util.Map;

class ShippingService {
    private final Map<String, String> shipments = new HashMap<>(); // its own store, not shared

    String schedule(Order order) {
        String tracking = "TRK-" + order.getId();
        shipments.put(order.getId(), tracking);
        return tracking;
    }
}
