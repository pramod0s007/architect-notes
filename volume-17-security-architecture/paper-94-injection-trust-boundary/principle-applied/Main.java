import java.util.ArrayList;
import java.util.List;

public class Main {
    // Parameterized lookup: the user's input is compared as DATA, using
    // .equals() against a real field value -- it is never concatenated into
    // anything that gets parsed as syntax. There is no query string for an
    // injected OR to attach itself to, because there is no query string.
    static List<Order> lookupByOrderId(List<Order> orders, String userInput) {
        List<Order> matches = new ArrayList<>();
        for (Order order : orders) {
            if (order.orderId.equals(userInput)) {
                matches.add(order);
            }
        }
        return matches;
    }

    public static void main(String[] args) {
        List<Order> orders = new ArrayList<>();
        orders.add(new Order("O100", "U1", "Alice's prescription order"));
        orders.add(new Order("O200", "U2", "Bob's confidential order"));
        orders.add(new Order("O300", "U3", "Carol's confidential order"));

        System.out.println("Honest lookup for orderId=O100:");
        List<Order> honest = lookupByOrderId(orders, "O100");
        for (Order o : honest) {
            System.out.println("  " + o.orderId + ": " + o.contents);
        }

        System.out.println();
        String attackInput = "nonexistent' OR '1'='1";
        System.out.println("Attacker's lookup input: " + attackInput);
        List<Order> attacked = lookupByOrderId(orders, attackInput);
        System.out.println("Records returned: " + attacked.size());

        System.out.println();
        System.out.println(attacked.isEmpty()
            ? "FIXED: the exact same malicious input returns zero records. It's compared as a literal "
              + "value against each order's real orderId, and no order's orderId is the string "
              + "\"nonexistent' OR '1'='1\" -- the quote, the OR, and the '1'='1' are all just characters "
              + "in a value now, with no mechanism anywhere that ever interprets them as syntax."
            : "Unexpected -- investigate.");
    }
}
