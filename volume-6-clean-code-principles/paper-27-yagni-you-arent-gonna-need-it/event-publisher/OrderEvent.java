/**
 * Simple event record carrying the details of a placed order.
 */
public class OrderEvent {

    private final String orderId;
    private final String type;
    private final double amount;

    public OrderEvent(String orderId, String type, double amount) {
        this.orderId = orderId;
        this.type = type;
        this.amount = amount;
    }

    public String getOrderId() { return orderId; }
    public String getType()    { return type; }
    public double getAmount()  { return amount; }

    @Override
    public String toString() {
        return "OrderEvent{orderId=" + orderId + ", type=" + type + ", amount=" + amount + "}";
    }
}
