// The eventId is generated once, at publish time, and carried unchanged
// through every delivery attempt -- including redeliveries of the same event.
class OrderEvent {
    private final String eventId;
    private final Order order;

    OrderEvent(String eventId, Order order) {
        this.eventId = eventId;
        this.order = order;
    }

    String getEventId() {
        return eventId;
    }

    Order getOrder() {
        return order;
    }
}
