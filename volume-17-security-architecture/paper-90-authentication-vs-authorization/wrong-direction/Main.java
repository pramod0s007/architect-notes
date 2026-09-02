import java.util.HashMap;
import java.util.Map;

public class Main {
    // Overcorrection: there IS now an authorization check -- it compares the
    // order's owner against a userId, so it isn't the pressure case's bug.
    // But the userId it compares against is CLAIMED BY THE REQUEST ITSELF,
    // not read from the session. The request is trusted to say who it's
    // acting as, instead of the server's own authenticated identity being
    // used for that comparison.
    static Order fetchOrder(Session session, Map<String, Order> orders, String orderId, String claimedUserId) {
        if (!session.valid) {
            throw new SecurityException("DENIED: not authenticated");
        }
        Order order = orders.get(orderId);
        if (!order.ownerId.equals(claimedUserId)) {
            throw new SecurityException("DENIED: claimed identity " + claimedUserId
                + " does not own " + orderId);
        }
        return order;
    }

    public static void main(String[] args) {
        Map<String, Order> orders = new HashMap<>();
        orders.put("O100", new Order("O100", "U1", "Alice's prescription order"));
        orders.put("O200", new Order("O200", "U2", "Bob's confidential order"));

        // Alice's session is genuinely, correctly authenticated as U1 --
        // exactly as in the fix. Nothing about her real identity changed.
        Session aliceSession = new Session("S1", "U1", true);

        System.out.println("Alice (session genuinely authenticated as U1) requests order O200,");
        System.out.println("but the request itself CLAIMS to be acting as U2 (Bob's real userId):");
        Order fetched = fetchOrder(aliceSession, orders, "O200", "U2");
        System.out.println("  Returned: " + fetched.contents);

        System.out.println();
        boolean authFailedToProtect = fetched.ownerId.equals("U2") && !aliceSession.authenticatedUserId.equals("U2");
        System.out.println(authFailedToProtect
            ? "CONFIRMED FAILURE: an authorization check ran, and it even matched correctly -- the order's "
              + "real owner (U2) equals the claimed userId (U2). It's just that 'claimed userId' came "
              + "from the request, which Alice controls completely, not from her actual authenticated "
              + "session (U1). Any authenticated user can access any resource in the entire system just "
              + "by claiming to be its owner in the request. This looks more secure than the pressure "
              + "case, because a check is visibly present -- and it protects nothing, because it checks "
              + "the attacker's own assertion instead of the one fact the server actually knows for sure."
            : "Unexpected -- investigate.");
    }
}
