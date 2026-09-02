public class Main {
    // Layer 1: the SAME buggy check as the pressure case. Defense in depth
    // doesn't require fixing every layer to be perfect -- it assumes any
    // single layer CAN be flawed, and that's exactly the assumption being
    // tested here.
    static boolean buggyAuthorize(Session session, Order order) {
        return session.valid && order.ownerId.equalsIgnoreCase(session.authenticatedUserId);
    }

    // Layer 2: a genuinely independent mechanism, checking the same
    // property (does this session own this order) with different logic --
    // strict, case-sensitive equality, implemented separately at the point
    // data actually leaves the system.
    static boolean strictOwnershipVerify(Session session, Order order) {
        return session.valid && order.ownerId.equals(session.authenticatedUserId);
    }

    static Order fetchOrder(Session session, Order order) {
        if (!buggyAuthorize(session, order)) {
            throw new SecurityException("DENIED at layer 1");
        }
        if (!strictOwnershipVerify(session, order)) {
            throw new SecurityException("DENIED at layer 2");
        }
        return order;
    }

    public static void main(String[] args) {
        Order aliceOrder = new Order("O100", "U1", "Alice's confidential order");
        Session attackerSession = new Session("S9", "u1", true);

        System.out.println("Attacker authenticated as 'u1' requests Alice's order, whose real owner is 'U1':");
        System.out.println("  Layer 1 (case-insensitive, buggy): " + buggyAuthorize(attackerSession, aliceOrder));
        System.out.println("  Layer 2 (case-sensitive, independent): " + strictOwnershipVerify(attackerSession, aliceOrder));

        boolean denied = false;
        String denialLayer = null;
        try {
            fetchOrder(attackerSession, aliceOrder);
        } catch (SecurityException e) {
            denied = true;
            denialLayer = e.getMessage();
        }

        System.out.println();
        System.out.println(denied
            ? "FIXED: " + denialLayer + ". Layer 1's real bug fired exactly as it did in the pressure "
              + "case -- it still approved the attacker. But layer 2 checks the identical property with "
              + "genuinely different logic, and case-sensitive equality correctly tells 'u1' and 'U1' "
              + "apart. The overall system stayed secure not because either layer was bug-free, but "
              + "because the two layers don't share the same blind spot."
            : "Unexpected -- investigate.");
    }
}
