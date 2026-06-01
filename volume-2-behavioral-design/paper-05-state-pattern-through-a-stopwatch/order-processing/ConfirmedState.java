/**
 * CONFIRMED — payment captured, order queued for fulfilment.
 * Valid transitions: ship, cancel.
 */
public final class ConfirmedState implements OrderState {

    @Override
    public void confirm(Order order) {
        throw new IllegalStateException("Order " + order.getId() + " is already confirmed");
    }

    @Override
    public void ship(Order order) {
        System.out.printf("[Order %s] CONFIRMED -> SHIPPED: dispatched from warehouse%n", order.getId());
        order.setState(new ShippedState());
    }

    @Override
    public void deliver(Order order) {
        throw new IllegalStateException("Cannot deliver order " + order.getId() + " — not yet shipped");
    }

    @Override
    public void cancel(Order order) {
        System.out.printf("[Order %s] CONFIRMED -> CANCELLED: cancelled after confirmation, refund queued%n", order.getId());
        order.setState(new CancelledState());
    }

    @Override
    public void refund(Order order) {
        throw new IllegalStateException("Cannot refund order " + order.getId() + " — not yet delivered");
    }

    @Override
    public String label() {
        return "CONFIRMED";
    }
}
