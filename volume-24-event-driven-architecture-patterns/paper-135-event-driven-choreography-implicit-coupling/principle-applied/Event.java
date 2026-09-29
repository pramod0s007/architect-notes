/** A single published event, identified by type, carrying the order it's about. */
public class Event {
    private final String type;
    private final String orderId;

    public Event(String type, String orderId) {
        this.type = type;
        this.orderId = orderId;
    }

    public String getType() {
        return type;
    }

    public String getOrderId() {
        return orderId;
    }
}
