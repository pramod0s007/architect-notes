import java.util.HashMap;
import java.util.Map;

public class Main {
    // Two separate checks, in order: (1) authentication -- is this a valid
    // session at all -- and (2) authorization -- does THIS session's own,
    // server-issued identity actually own the resource being requested.
    // The comparison uses session.authenticatedUserId, which the request
    // itself has no way to influence.
    static Order fetchOrder(Session session, Map<String, Order> orders, String orderId) {
        if (!session.valid) {
            throw new SecurityException("DENIED: not authenticated");
        }
        Order order = orders.get(orderId);
        if (!order.ownerId.equals(session.authenticatedUserId)) {
            throw new SecurityException("DENIED: authenticated as " + session.authenticatedUserId
                + ", not authorized for " + orderId + " (owned by " + order.ownerId + ")");
        }
        return order;
    }

    public static void main(String[] args) {
        Map<String, Order> orders = new HashMap<>();
        orders.put("O100", new Order("O100", "U1", "Alice's prescription order"));
        orders.put("O200", new Order("O200", "U2", "Bob's confidential order"));

        Session aliceSession = new Session("S1", "U1", true);

        System.out.println("Alice (authenticated as U1) requests her own order O100:");
        Order own = fetchOrder(aliceSession, orders, "O100");
        System.out.println("  Returned: " + own.contents);

        System.out.println();
        System.out.println("Alice (still authenticated as U1) requests order O200, which belongs to U2:");
        boolean denied = false;
        String denialMessage = null;
        try {
            fetchOrder(aliceSession, orders, "O200");
        } catch (SecurityException e) {
            denied = true;
            denialMessage = e.getMessage();
        }
        System.out.println("  " + denialMessage);

        System.out.println();
        System.out.println(denied
            ? "FIXED: Alice's session is authenticated correctly -- and separately, correctly denied "
              + "access to an order she doesn't own. The authorization check compared the order's real "
              + "owner against the session's own server-issued identity, which the request has no way to "
              + "override. Being logged in and being allowed are two different questions, and both were "
              + "actually asked this time."
            : "Unexpected -- investigate.");
    }
}
