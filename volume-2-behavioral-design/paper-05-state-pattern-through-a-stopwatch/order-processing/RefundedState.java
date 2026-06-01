/**
 * REFUNDED — terminal state. Credit has been issued; lifecycle is complete.
 */
public final class RefundedState implements OrderState {

    @Override
    public void confirm(Order order) {
        throw new IllegalStateException("Order " + order.getId() + " is refunded — lifecycle complete");
    }

    @Override
    public void ship(Order order) {
        throw new IllegalStateException("Order " + order.getId() + " is refunded — lifecycle complete");
    }

    @Override
    public void deliver(Order order) {
        throw new IllegalStateException("Order " + order.getId() + " is refunded — lifecycle complete");
    }

    @Override
    public void cancel(Order order) {
        throw new IllegalStateException("Order " + order.getId() + " is refunded — lifecycle complete");
    }

    @Override
    public void refund(Order order) {
        throw new IllegalStateException("Order " + order.getId() + " is already refunded");
    }

    @Override
    public String label() {
        return "REFUNDED";
    }
}
