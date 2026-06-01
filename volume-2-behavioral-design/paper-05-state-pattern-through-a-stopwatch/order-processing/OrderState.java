/**
 * State interface — each concrete state owns the valid transitions out of itself.
 * Invalid transitions throw {@link IllegalStateException} rather than silently ignoring.
 *
 * See: State pattern — behavior that changes based on internal state.
 */
public interface OrderState {

    void confirm(Order order);

    void ship(Order order);

    void deliver(Order order);

    void cancel(Order order);

    void refund(Order order);

    /** Display name used in logs and receipts. */
    String label();
}
