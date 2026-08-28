import java.util.HashMap;
import java.util.Map;

class PaymentRouter {
    private final Map<String, Boolean> charges = new HashMap<>();

    boolean charge(Order order) {
        charges.put(order.getId(), true);
        return true;
    }

    boolean isCharged(Order order) {
        return charges.getOrDefault(order.getId(), false);
    }
}
