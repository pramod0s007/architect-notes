/**
 * PENDING — order placed, payment not yet confirmed.
 * Valid transitions: confirm, cancel.
 */
public final class PendingState implements OrderState {

    @Override
    public void confirm(Order order) {
        System.out.printf("[Order %s] PENDING -> CONFIRMED: payment accepted%n", order.getId());
        order.setState(new ConfirmedState());
    }

    @Override
    public void ship(Order order) {
        throw new IllegalStateException("Cannot ship an unconfirmed order " + order.getId());
    }

    @Override
    public void deliver(Order order) {
        throw new IllegalStateException("Cannot deliver an unconfirmed order " + order.getId());
    }

    @Override
    public void cancel(Order order) {
        System.out.printf("[Order %s] PENDING -> CANCELLED: cancelled before confirmation%n", order.getId());
        order.setState(new CancelledState());
    }

    @Override
    public void refund(Order order) {
        throw new IllegalStateException("Cannot refund a pending order " + order.getId() + " — not yet charged");
    }

    @Override
    public String label() {
        return "PENDING";
    }
}
