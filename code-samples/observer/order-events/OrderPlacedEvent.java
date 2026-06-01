package observer.orderevents;

import java.time.Instant;

public class OrderPlacedEvent {
    private final Order order;
    private final Instant occurredAt;

    public OrderPlacedEvent(Order order) {
        this.order = order;
        this.occurredAt = Instant.now();
    }

    public Order getOrder() { return order; }
    public Instant getOccurredAt() { return occurredAt; }
}
