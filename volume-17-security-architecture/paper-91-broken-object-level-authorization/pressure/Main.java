import java.util.HashMap;
import java.util.Map;

public class Main {
    // readOrder was written first and correctly checks ownership -- this is
    // Paper 90's fix, already in place here.
    static Order readOrder(Session session, Map<String, Order> orders, String orderId) {
        if (!session.valid) {
            throw new SecurityException("DENIED: not authenticated");
        }
        Order order = orders.get(orderId);
        if (!order.ownerId.equals(session.authenticatedUserId)) {
            throw new SecurityException("DENIED: not authorized to read " + orderId);
        }
        return order;
    }

    // updateStatus was added later, by someone focused on making the update
    // work correctly -- it checks authentication, but the ownership check
    // that readOrder has was never copied over. Same resource. Different
    // operation. Different, incomplete enforcement.
    static void updateStatus(Session session, Map<String, Order> orders, String orderId, String newStatus) {
        if (!session.valid) {
            throw new SecurityException("DENIED: not authenticated");
        }
        Order order = orders.get(orderId);
        order.status = newStatus; // no ownership check at all
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
        updateStatus(aliceSession, orders, "O200", "CANCELLED");
        System.out.println("  O200 status is now: " + orders.get("O200").status);

        System.out.println();
        boolean writeSucceededAnyway = orders.get("O200").status.equals("CANCELLED");
        System.out.println(readDenied && writeSucceededAnyway
            ? "CONFIRMED FAILURE: reading O200 was correctly denied -- and updating the exact same "
              + "order, by the exact same unauthorized user, succeeded completely. The ownership check "
              + "exists in this codebase; it just wasn't applied to every operation on the resource. "
              + "Authorization isn't a property of a resource, it's a property of every single access "
              + "path to that resource, and this one had two."
            : "Unexpected -- investigate.");
    }
}
