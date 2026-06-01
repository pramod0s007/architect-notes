/**
 * DELIVERED — customer has received the package.
 * Valid transitions: refund (return/dispute window is open).
 */
public final class DeliveredState implements OrderState {

    @Override
    public void confirm(Order order) {
        throw new IllegalStateException("Order " + order.getId() + " is already confirmed");
    }

    @Override
    public void ship(Order order) {
        throw new IllegalStateException("Order " + order.getId() + " is already shipped");
    }

    @Override
    public void deliver(Order order) {
        throw new IllegalStateException("Order " + order.getId() + " is already delivered");
    }

    @Override
    public void cancel(Order order) {
        throw new IllegalStateException("Cannot cancel order " + order.getId() + " — already delivered; use refund");
    }

    @Override
    public void refund(Order order) {
        System.out.printf("[Order %s] DELIVERED -> REFUNDED: return accepted, credit issued%n", order.getId());
        order.setState(new RefundedState());
    }

    @Override
    public String label() {
        return "DELIVERED";
    }
}
