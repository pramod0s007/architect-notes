/**
 * Order — context object. Holds current state and delegates all lifecycle
 * operations to it. The transition logic lives in the state, not here.
 */
public final class Order {

    private final String id;
    private final String customerEmail;
    private OrderState state;

    public Order(String id, String customerEmail) {
        this.id = id;
        this.customerEmail = customerEmail;
        this.state = new PendingState();
        System.out.printf("[Order %s] Created for %s — state: %s%n", id, customerEmail, state.label());
    }

    // --- Lifecycle operations (all delegated to current state) ---

    public void confirm()  { state.confirm(this); }
    public void ship()     { state.ship(this); }
    public void deliver()  { state.deliver(this); }
    public void cancel()   { state.cancel(this); }
    public void refund()   { state.refund(this); }

    // --- State accessor (called by concrete states during transitions) ---

    /** Package-private: only state classes should call this. */
    void setState(OrderState newState) {
        this.state = newState;
    }

    // --- Getters ---

    public String getId()            { return id; }
    public String getCustomerEmail() { return customerEmail; }
    public String getStatus()        { return state.label(); }
}
