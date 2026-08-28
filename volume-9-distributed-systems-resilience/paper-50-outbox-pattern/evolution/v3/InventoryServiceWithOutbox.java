import java.util.HashMap;
import java.util.Map;

// The local write and the event both land in this method -- one local
// transaction, same as any single-service write. Publishing to the broker
// is a completely separate concern, handled later by the OutboxRelay.
class InventoryServiceWithOutbox {
    private final Map<String, Boolean> reservations = new HashMap<>();
    private final Outbox outbox;

    InventoryServiceWithOutbox(Outbox outbox) {
        this.outbox = outbox;
    }

    boolean reserve(Order order) {
        reservations.put(order.getId(), true);
        outbox.add(new OutboxEntry("InventoryReserved", order.getId()));
        return true;
    }

    boolean isReserved(Order order) {
        return reservations.getOrDefault(order.getId(), false);
    }
}
