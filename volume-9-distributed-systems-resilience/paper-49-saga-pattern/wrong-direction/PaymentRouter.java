import java.util.HashMap;
import java.util.Map;

class PaymentRouter {
    private final Map<String, Boolean> charges = new HashMap<>();

    boolean charge(Order order) {
        charges.put(order.getId(), true);
        return true;
    }

    void refund(Order order) {
        charges.put(order.getId(), false);
    }

    boolean isCharged(Order order) {
        return charges.getOrDefault(order.getId(), false);
    }
}
