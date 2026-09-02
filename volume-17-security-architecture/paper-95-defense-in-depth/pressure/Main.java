public class Main {
    // A real authorization check, with a real, subtle bug: it compares
    // ownership case-INSENSITIVELY, presumably written that way for
    // developer convenience during testing and never revisited. "U1" and
    // "u1" are two different, independently registered accounts -- this
    // check can't tell them apart.
    static boolean buggyAuthorize(Session session, Order order) {
        return session.valid && order.ownerId.equalsIgnoreCase(session.authenticatedUserId);
    }

    static Order fetchOrder(Session session, Order order) {
        if (!buggyAuthorize(session, order)) {
            throw new SecurityException("DENIED");
        }
        return order;
    }

    public static void main(String[] args) {
        Order aliceOrder = new Order("O100", "U1", "Alice's confidential order");

        // A completely different person registers an account with a userId
        // that happens to match Alice's, differing only in case.
        Session attackerSession = new Session("S9", "u1", true);

        System.out.println("Attacker authenticated as 'u1' (a real, distinct, independently registered account)");
        System.out.println("requests Alice's order, whose real owner is 'U1':");
        Order fetched = fetchOrder(attackerSession, aliceOrder);
        System.out.println("  Returned: " + fetched.contents);

        System.out.println();
        System.out.println(fetched == aliceOrder
            ? "CONFIRMED FAILURE: this is the only check standing between the attacker and Alice's data, "
              + "and it has a real, specific bug -- case-insensitive comparison treats 'u1' and 'U1' as "
              + "the same owner. With exactly one layer of defense, that one bug is the entire security "
              + "posture. There was nothing else in place to catch what this check missed."
            : "Unexpected -- investigate.");
    }
}
