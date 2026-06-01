/**
 * CANCELLED — terminal state. No further transitions are valid.
 */
public final class CancelledState implements OrderState {

    @Override
    public void confirm(Order order) {
        throw new IllegalStateException("Order " + order.getId() + " is cancelled and cannot be confirmed");
    }

    @Override
    public void ship(Order order) {
        throw new IllegalStateException("Order " + order.getId() + " is cancelled and cannot be shipped");
    }

    @Override
    public void deliver(Order order) {
        throw new IllegalStateException("Order " + order.getId() + " is cancelled and cannot be delivered");
    }

    @Override
    public void cancel(Order order) {
        throw new IllegalStateException("Order " + order.getId() + " is already cancelled");
    }

    @Override
    public void refund(Order order) {
        throw new IllegalStateException("Order " + order.getId() + " is cancelled — no charge was taken");
    }

    @Override
    public String label() {
        return "CANCELLED";
    }
}
