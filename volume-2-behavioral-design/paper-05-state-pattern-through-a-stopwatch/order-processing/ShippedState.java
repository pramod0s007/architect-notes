/**
 * SHIPPED — package in transit, carrier has possession.
 * Valid transitions: deliver.
 */
public final class ShippedState implements OrderState {

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
        System.out.printf("[Order %s] SHIPPED -> DELIVERED: confirmed by customer%n", order.getId());
        order.setState(new DeliveredState());
    }

    @Override
    public void cancel(Order order) {
        throw new IllegalStateException("Cannot cancel order " + order.getId() + " — already in transit");
    }

    @Override
    public void refund(Order order) {
        throw new IllegalStateException("Cannot refund order " + order.getId() + " — wait for delivery first");
    }

    @Override
    public String label() {
        return "SHIPPED";
    }
}
