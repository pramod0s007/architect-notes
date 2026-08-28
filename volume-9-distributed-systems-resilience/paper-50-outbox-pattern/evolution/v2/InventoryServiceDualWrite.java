import java.util.HashMap;
import java.util.Map;

// Two separate writes: the local reservation, and the event telling Shipping
// about it. Nothing makes these two writes succeed or fail together.
class InventoryServiceDualWrite {
    private final Map<String, Boolean> reservations = new HashMap<>();
    private final MessageBroker broker;

    InventoryServiceDualWrite(MessageBroker broker) {
        this.broker = broker;
    }

    boolean reserve(Order order) {
        reservations.put(order.getId(), true); // write #1: local state -- always succeeds here
        broker.publish("InventoryReserved", order.getId()); // write #2: the event -- may fail independently
        return true;
    }

    boolean isReserved(Order order) {
        return reservations.getOrDefault(order.getId(), false);
    }
}
