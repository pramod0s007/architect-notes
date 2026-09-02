import java.util.HashMap;
import java.util.Map;

public class Main {
    // Overcorrection: the ownership check IS centralized -- there's exactly
    // one implementation, exactly like the fix. But it lives in a gateway
    // wrapper that sits in front of the operations, not inside the
    // operations themselves. readOrder/updateStatus below trust that
    // whoever calls them already went through the gateway.
    static Order readOrder(Map<String, Order> orders, String orderId) {
        return orders.get(orderId); // no check -- trusts the caller already authorized this
    }

    static void updateStatus(Map<String, Order> orders, String orderId, String newStatus) {
        orders.get(orderId).status = newStatus; // no check here either
    }

    static class Gateway {
        static Order read(Session session, Map<String, Order> orders, String orderId) {
            checkOwnership(session, orders.get(orderId));
            return readOrder(orders, orderId);
        }

        static void update(Session session, Map<String, Order> orders, String orderId, String newStatus) {
            checkOwnership(session, orders.get(orderId));
            updateStatus(orders, orderId, newStatus);
        }

        static void checkOwnership(Session session, Order order) {
            if (!session.valid || !order.ownerId.equals(session.authenticatedUserId)) {
                throw new SecurityException("DENIED: not authorized for " + order.orderId);
            }
        }
    }

    public static void main(String[] args) {
        Map<String, Order> orders = new HashMap<>();
        orders.put("O100", new Order("O100", "U1", "PENDING"));
        orders.put("O200", new Order("O200", "U2", "PENDING"));

        Session aliceSession = new Session("S1", "U1", true);

        System.out.println("Alice, through the Gateway (the normal external HTTP path), tries to update O200:");
        boolean gatewayDenied = false;
        try {
            Gateway.update(aliceSession, orders, "O200", "CANCELLED");
        } catch (SecurityException e) {
            gatewayDenied = true;
            System.out.println("  " + e.getMessage());
        }

        System.out.println();
        System.out.println("An internal batch job calls updateStatus(...) DIRECTLY, bypassing the Gateway entirely");
        System.out.println("(a real, second access path to the same operation -- e.g. an internal service call,");
        System.out.println("a cron job, or an admin tool that was never routed through the HTTP layer):");
        updateStatus(orders, "O200", "CANCELLED");
        System.out.println("  O200 status is now: " + orders.get("O200").status);

        System.out.println();
        boolean bypassSucceeded = orders.get("O200").status.equals("CANCELLED");
        System.out.println(gatewayDenied && bypassSucceeded
            ? "CONFIRMED FAILURE: the Gateway-routed path was correctly denied -- the centralized check "
              + "works exactly as designed for the traffic that goes through it. The direct internal "
              + "call bypassed it completely, because the check lives at the edge instead of inside the "
              + "operation itself, and updateStatus() has no memory of ever needing authorization. "
              + "Centralizing a check is only as good as guaranteeing every real caller goes through it -- "
              + "and 'every external HTTP request' is a smaller set than 'every caller.'"
            : "Unexpected -- investigate.");
    }
}
