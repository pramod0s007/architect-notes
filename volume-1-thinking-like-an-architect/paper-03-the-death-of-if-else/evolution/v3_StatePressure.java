// =============================================================================
// v3 — STATE PRESSURE: Payment Status Transitions
// =============================================================================
// Domain: Payment Processing
// Pressure type: STATE — the number of states and legal transitions grows
//
// Month 1: 2 states — INITIATED, COMPLETED
// Month 3: FAILED added (payment can fail after initiation)
// Month 5: PROCESSING added (async gateway adds intermediate state)
// Month 6: AUTHORIZED added (two-step capture: auth then capture)
// Month 7: CAPTURED added (separate from AUTHORIZED)
// Month 8: REFUNDING added (refund is not instant)
// Month 9: REFUNDED added (terminal refund state)
//
// SIGNAL that this is STATE pressure (not behavior, not rules):
//   - The if-else grows because we check the CURRENT STATE before every action
//   - Same action (e.g. cancel()) has different behavior depending on current state:
//       cancel() on INITIATED  → allowed, void the payment
//       cancel() on AUTHORIZED → allowed, void the authorization
//       cancel() on CAPTURED   → NOT allowed, must refund instead
//       cancel() on REFUNDED   → NOT allowed, already terminal
//   - "What can I do from this state?" is a state-machine question
//
// Solution preview: State Pattern (State objects encode both the allowed
// transitions AND the behavior per action)
// =============================================================================

public class v3_StatePressure {

    // ---------------------------------------------------------------------------
    // Month 1 — Two states, integer flags. Correct for now.
    // ---------------------------------------------------------------------------
    static class PaymentV1 {
        // status: 0 = INITIATED, 1 = COMPLETED
        private int status = 0;
        private final String id;

        public PaymentV1(String id) { this.id = id; }

        public void complete() {
            if (status == 0) {
                status = 1;
                System.out.println("[" + id + "] INITIATED → COMPLETED");
            } else {
                System.out.println("[" + id + "] Cannot complete from status=" + status);
            }
        }

        public void cancel() {
            if (status == 0) {
                System.out.println("[" + id + "] INITIATED → CANCELLED (voided)");
            } else {
                System.out.println("[" + id + "] Cannot cancel from status=" + status);
            }
        }
    }

    // ---------------------------------------------------------------------------
    // Month 9 — Seven states. Every method checks 7 branches. STATE PRESSURE.
    //
    // [!] process() has 7 branches — only valid from INITIATED
    // [!] authorize() has 7 branches — only valid from PROCESSING
    // [!] capture() has 7 branches — only valid from AUTHORIZED
    // [!] fail() has 7 branches — valid from PROCESSING or AUTHORIZED
    // [!] refund() has 7 branches — only valid from CAPTURED
    // [!] cancel() has 7 branches — valid from INITIATED, PROCESSING, AUTHORIZED
    //     but NOT from CAPTURED (must use refund), NOT from terminal states
    //
    // The total branch count = 6 actions × 7 states = 42 conditional checks
    // Each new state forces 6 existing methods to be reopened and modified
    //
    // [!] Illegal transitions are caught at runtime by if-else — no compile
    //     safety. A developer can call capture() from INITIATED without warning.
    // ---------------------------------------------------------------------------
    enum PaymentStatusV2 {
        INITIATED, PROCESSING, AUTHORIZED, CAPTURED, FAILED, REFUNDING, REFUNDED
    }

    static class PaymentV2 {
        private PaymentStatusV2 status = PaymentStatusV2.INITIATED;
        private final String id;

        public PaymentV2(String id) { this.id = id; }

        // [!] Each method repeats the same status-checking pattern
        public void process() {
            if (status == PaymentStatusV2.INITIATED) {
                status = PaymentStatusV2.PROCESSING;
                System.out.println("[" + id + "] INITIATED → PROCESSING");
            } else {
                System.out.println("[" + id + "] Cannot process from " + status);
            }
        }

        public void authorize() {
            if (status == PaymentStatusV2.PROCESSING) {
                status = PaymentStatusV2.AUTHORIZED;
                System.out.println("[" + id + "] PROCESSING → AUTHORIZED");
            } else {
                System.out.println("[" + id + "] Cannot authorize from " + status);
            }
        }

        public void capture() {
            if (status == PaymentStatusV2.AUTHORIZED) {
                status = PaymentStatusV2.CAPTURED;
                System.out.println("[" + id + "] AUTHORIZED → CAPTURED");
            } else {
                System.out.println("[" + id + "] Cannot capture from " + status);
            }
        }

        public void fail() {
            if (status == PaymentStatusV2.PROCESSING || status == PaymentStatusV2.AUTHORIZED) {
                status = PaymentStatusV2.FAILED;
                System.out.println("[" + id + "] " + status + " → FAILED");
            } else {
                System.out.println("[" + id + "] Cannot fail from " + status);
            }
        }

        public void refund() {
            if (status == PaymentStatusV2.CAPTURED) {
                status = PaymentStatusV2.REFUNDING;
                System.out.println("[" + id + "] CAPTURED → REFUNDING");
            } else if (status == PaymentStatusV2.REFUNDING) {
                status = PaymentStatusV2.REFUNDED;
                System.out.println("[" + id + "] REFUNDING → REFUNDED");
            } else {
                System.out.println("[" + id + "] Cannot refund from " + status);
            }
        }

        public void cancel() {
            // [!] cancel() is the worst: valid from 3 states, each with different action
            if (status == PaymentStatusV2.INITIATED) {
                System.out.println("[" + id + "] INITIATED → voided (cancel ok)");
            } else if (status == PaymentStatusV2.PROCESSING) {
                System.out.println("[" + id + "] PROCESSING → voided (cancel ok, notify gateway)");
            } else if (status == PaymentStatusV2.AUTHORIZED) {
                System.out.println("[" + id + "] AUTHORIZED → void auth (cancel ok, release hold)");
            } else if (status == PaymentStatusV2.CAPTURED) {
                System.out.println("[" + id + "] CAPTURED → cannot cancel, use refund() instead");
            } else {
                System.out.println("[" + id + "] Cannot cancel from terminal state " + status);
            }
        }

        public PaymentStatusV2 getStatus() { return status; }
    }

    // ---------------------------------------------------------------------------
    // v3 — State Pattern applied
    //
    // WHAT CHANGED:
    //   - PaymentState interface: each action is a method
    //   - 7 concrete state classes — each encodes what's legal FROM that state
    //   - Payment class delegates all actions to its current state object
    //   - Illegal transitions throw a clear exception from the state itself
    //   - Adding an 8th state = new class + update only the states that
    //     transition INTO it. All other states unchanged.
    //
    // WHY State Pattern (not Strategy):
    //   - The variation is ACROSS ACTIONS within each state, not within one action
    //   - Strategy encapsulates ONE algorithm; State encapsulates ALL behavior
    //     for a given state (process, authorize, capture, fail, refund, cancel)
    //   - The pattern prevents illegal transitions at compile time (you can't
    //     call CapturedState.capture() — it throws immediately with a clear message)
    //   - State transitions are now inside state objects — the transition graph
    //     is explicit and auditable
    // ---------------------------------------------------------------------------
    interface PaymentState {
        void process(PaymentV3 ctx);
        void authorize(PaymentV3 ctx);
        void capture(PaymentV3 ctx);
        void fail(PaymentV3 ctx);
        void refund(PaymentV3 ctx);
        void cancel(PaymentV3 ctx);
        String name();
    }

    // Base class to avoid boilerplate — default is "illegal from this state"
    static abstract class AbstractPaymentState implements PaymentState {
        @Override public void process(PaymentV3 ctx)   { illegal("process"); }
        @Override public void authorize(PaymentV3 ctx) { illegal("authorize"); }
        @Override public void capture(PaymentV3 ctx)   { illegal("capture"); }
        @Override public void fail(PaymentV3 ctx)      { illegal("fail"); }
        @Override public void refund(PaymentV3 ctx)    { illegal("refund"); }
        @Override public void cancel(PaymentV3 ctx)    { illegal("cancel"); }

        private void illegal(String action) {
            throw new IllegalStateException("Cannot perform '" + action + "' from state " + name());
        }
    }

    static class InitiatedState extends AbstractPaymentState {
        @Override public String name() { return "INITIATED"; }
        @Override public void process(PaymentV3 ctx) {
            System.out.println("[" + ctx.getId() + "] INITIATED → PROCESSING");
            ctx.setState(new ProcessingState());
        }
        @Override public void cancel(PaymentV3 ctx) {
            System.out.println("[" + ctx.getId() + "] INITIATED → voided");
            ctx.setState(new CancelledState());
        }
    }

    static class ProcessingState extends AbstractPaymentState {
        @Override public String name() { return "PROCESSING"; }
        @Override public void authorize(PaymentV3 ctx) {
            System.out.println("[" + ctx.getId() + "] PROCESSING → AUTHORIZED");
            ctx.setState(new AuthorizedState());
        }
        @Override public void fail(PaymentV3 ctx) {
            System.out.println("[" + ctx.getId() + "] PROCESSING → FAILED");
            ctx.setState(new FailedState());
        }
        @Override public void cancel(PaymentV3 ctx) {
            System.out.println("[" + ctx.getId() + "] PROCESSING → voided (gateway notified)");
            ctx.setState(new CancelledState());
        }
    }

    static class AuthorizedState extends AbstractPaymentState {
        @Override public String name() { return "AUTHORIZED"; }
        @Override public void capture(PaymentV3 ctx) {
            System.out.println("[" + ctx.getId() + "] AUTHORIZED → CAPTURED");
            ctx.setState(new CapturedState());
        }
        @Override public void fail(PaymentV3 ctx) {
            System.out.println("[" + ctx.getId() + "] AUTHORIZED → FAILED");
            ctx.setState(new FailedState());
        }
        @Override public void cancel(PaymentV3 ctx) {
            System.out.println("[" + ctx.getId() + "] AUTHORIZED → void auth (hold released)");
            ctx.setState(new CancelledState());
        }
    }

    static class CapturedState extends AbstractPaymentState {
        @Override public String name() { return "CAPTURED"; }
        @Override public void refund(PaymentV3 ctx) {
            System.out.println("[" + ctx.getId() + "] CAPTURED → REFUNDING");
            ctx.setState(new RefundingState());
        }
        // cancel() from CAPTURED throws — "use refund() instead" is encoded in the exception
    }

    static class RefundingState extends AbstractPaymentState {
        @Override public String name() { return "REFUNDING"; }
        @Override public void refund(PaymentV3 ctx) {
            System.out.println("[" + ctx.getId() + "] REFUNDING → REFUNDED");
            ctx.setState(new RefundedState());
        }
    }

    static class RefundedState extends AbstractPaymentState {
        @Override public String name() { return "REFUNDED"; }
        // All actions throw — terminal state
    }

    static class FailedState extends AbstractPaymentState {
        @Override public String name() { return "FAILED"; }
        // All actions throw — terminal state
    }

    static class CancelledState extends AbstractPaymentState {
        @Override public String name() { return "CANCELLED"; }
        // All actions throw — terminal state
    }

    static class PaymentV3 {
        private PaymentState state = new InitiatedState();
        private final String id;

        public PaymentV3(String id) { this.id = id; }

        // Delegation — no if-else anywhere in this class
        public void process()   { state.process(this); }
        public void authorize() { state.authorize(this); }
        public void capture()   { state.capture(this); }
        public void fail()      { state.fail(this); }
        public void refund()    { state.refund(this); }
        public void cancel()    { state.cancel(this); }

        public void setState(PaymentState state) { this.state = state; }
        public String getStatus()                { return state.name(); }
        public String getId()                    { return id; }
    }

    public static void main(String[] args) {
        // v1 — two states
        System.out.println("=== v1 (2 states) ===");
        PaymentV1 p1 = new PaymentV1("TXN-001");
        p1.complete();
        p1.complete(); // second call — already completed

        // v2 — seven states, if-else pain
        System.out.println("\n=== v2 (7 states, if-else) ===");
        PaymentV2 p2 = new PaymentV2("TXN-002");
        p2.process();
        p2.authorize();
        p2.capture();
        p2.cancel();   // illegal from CAPTURED — prints warning
        p2.refund();   // correct path
        p2.refund();   // REFUNDING → REFUNDED

        // v3 — State Pattern
        System.out.println("\n=== v3 (State Pattern, happy path) ===");
        PaymentV3 p3 = new PaymentV3("TXN-003");
        p3.process();
        p3.authorize();
        p3.capture();
        p3.refund();
        p3.refund(); // REFUNDING → REFUNDED
        System.out.println("Final state: " + p3.getStatus());

        System.out.println("\n=== v3 (cancel at each stage) ===");
        PaymentV3 p4 = new PaymentV3("TXN-004");
        p4.cancel(); // INITIATED cancel — ok

        PaymentV3 p5 = new PaymentV3("TXN-005");
        p5.process();
        p5.authorize();
        p5.cancel(); // AUTHORIZED cancel — releases hold

        System.out.println("\n=== v3 (illegal transition caught) ===");
        PaymentV3 p6 = new PaymentV3("TXN-006");
        p6.process();
        p6.authorize();
        p6.capture();
        try {
            p6.cancel(); // CAPTURED.cancel() → throws
        } catch (IllegalStateException e) {
            System.out.println("Caught: " + e.getMessage()); // clear message
        }
    }
}
