import java.util.HashMap;
import java.util.Map;

public class Main {
    // ONE shared authorization check, called by every operation that
    // touches an order. Adding a new operation later means calling this,
    // not re-implementing (and possibly forgetting) the same logic again.
    static void requireOwnership(Session session, Order order) {
        if (!session.valid) {
            throw new SecurityException("DENIED: not authenticated");
        }
        if (!order.ownerId.equals(session.authenticatedUserId)) {
            throw new SecurityException("DENIED: not authorized for " + order.orderId);
        }
    }

    static Order readOrder(Session session, Map<String, Order> orders, String orderId) {
        Order order = orders.get(orderId);
        requireOwnership(session, order);
        return order;
    }

    static void updateStatus(Session session, Map<String, Order> orders, String orderId, String newStatus) {
        Order order = orders.get(orderId);
        requireOwnership(session, order); // same check, same code path, cannot be skipped separately
        order.status = newStatus;
    }

    public static void main(String[] args) {
        Map<String, Order> orders = new HashMap<>();
        orders.put("O100", new Order("O100", "U1", "PENDING"));
        orders.put("O200", new Order("O200", "U2", "PENDING"));

        Session aliceSession = new Session("S1", "U1", true);

        System.out.println("Alice (U1) tries to READ Bob's order O200:");
        boolean readDenied = false;
        try {
            readOrder(aliceSession, orders, "O200");
        } catch (SecurityException e) {
            readDenied = true;
            System.out.println("  " + e.getMessage());
        }

        System.out.println();
        System.out.println("Alice (U1) tries to UPDATE Bob's order O200's status to CANCELLED:");
        boolean updateDenied = false;
        try {
            updateStatus(aliceSession, orders, "O200", "CANCELLED");
        } catch (SecurityException e) {
            updateDenied = true;
            System.out.println("  " + e.getMessage());
        }

        System.out.println();
        System.out.println("O200 status is still: " + orders.get("O200").status);
        System.out.println();
        System.out.println(readDenied && updateDenied
            ? "FIXED: both operations were denied identically, because both call the exact same "
              + "ownership check on the exact same order before doing anything else. There was never a "
              + "second implementation of the check to forget -- there's exactly one, and every "
              + "operation on this resource goes through it."
            : "Unexpected -- investigate.");
    }
}
