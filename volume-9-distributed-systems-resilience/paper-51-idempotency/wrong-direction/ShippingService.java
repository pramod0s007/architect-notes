import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ShippingService {
    private final Map<String, List<String>> shipments = new HashMap<>();

    String schedule(Order order) {
        String tracking = "TRK-" + order.getId() + "-" + shipmentCount(order);
        shipments.computeIfAbsent(order.getId(), k -> new ArrayList<>()).add(tracking);
        return tracking;
    }

    int shipmentCount(Order order) {
        return shipments.getOrDefault(order.getId(), new ArrayList<>()).size();
    }
}
