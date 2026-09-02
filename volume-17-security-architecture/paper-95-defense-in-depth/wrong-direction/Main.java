public class Main {
    // Overcorrection: THREE separately-named checks, called in sequence --
    // it looks like defense in depth in a code review, three distinct gates
    // guarding the same resource. But all three are the identical
    // case-insensitive comparison, just written three times.
    static boolean gatewayCheck(Session session, Order order) {
        return session.valid && order.ownerId.equalsIgnoreCase(session.authenticatedUserId);
    }

    static boolean serviceLayerCheck(Session session, Order order) {
        return session.valid && order.ownerId.equalsIgnoreCase(session.authenticatedUserId);
    }

    static boolean dataAccessCheck(Session session, Order order) {
        return session.valid && order.ownerId.equalsIgnoreCase(session.authenticatedUserId);
    }

    static Order fetchOrder(Session session, Order order) {
        if (!gatewayCheck(session, order)) {
            throw new SecurityException("DENIED at gateway");
        }
        if (!serviceLayerCheck(session, order)) {
            throw new SecurityException("DENIED at service layer");
        }
        if (!dataAccessCheck(session, order)) {
            throw new SecurityException("DENIED at data access layer");
        }
        return order;
    }

    public static void main(String[] args) {
        Order aliceOrder = new Order("O100", "U1", "Alice's confidential order");
        Session attackerSession = new Session("S9", "u1", true);

        System.out.println("Attacker authenticated as 'u1' requests Alice's order, whose real owner is 'U1':");
        System.out.println("  Gateway check:      " + gatewayCheck(attackerSession, aliceOrder));
        System.out.println("  Service layer check: " + serviceLayerCheck(attackerSession, aliceOrder));
        System.out.println("  Data access check:  " + dataAccessCheck(attackerSession, aliceOrder));

        Order fetched = fetchOrder(attackerSession, aliceOrder);
        System.out.println();
        System.out.println("Returned despite three checks: " + fetched.contents);

        System.out.println();
        System.out.println(fetched == aliceOrder
            ? "CONFIRMED FAILURE: three separately-named checks, at three separately-named layers, all "
              + "returned the same wrong answer, because all three run the identical case-insensitive "
              + "comparison. Three checks provided exactly the protection of one check, run three times, "
              + "because none of them was implemented differently enough to catch what the others missed. "
              + "Counting layers is not the same as verifying they're independent."
            : "Unexpected -- investigate.");
    }
}
