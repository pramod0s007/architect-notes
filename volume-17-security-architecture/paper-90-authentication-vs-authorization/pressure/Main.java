import java.util.HashMap;
import java.util.Map;

public class Main {
    // Naive endpoint: checks only that the SESSION is authenticated, then
    // returns whatever order was asked for. It never asks "does this
    // authenticated user actually own this order?"
    static Order fetchOrder(Session session, Map<String, Order> orders, String orderId) {
        if (!session.valid) {
            throw new SecurityException("DENIED: not authenticated");
        }
        return orders.get(orderId); // returned unconditionally once authenticated
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
        Order others = fetchOrder(aliceSession, orders, "O200");
        System.out.println("  Returned: " + others.contents);

        System.out.println();
        System.out.println(!others.ownerId.equals(aliceSession.authenticatedUserId)
            ? "CONFIRMED FAILURE: Alice's session is genuinely, correctly authenticated -- and it just "
              + "retrieved Bob's private order by changing one ID in the request. The endpoint checked "
              + "'is this session valid' and treated that as sufficient; it never checked 'does this "
              + "session's user own the thing being requested.' Authentication answered a real question "
              + "correctly. It was just the wrong question to stop at."
            : "Unexpected -- investigate.");
    }
}
