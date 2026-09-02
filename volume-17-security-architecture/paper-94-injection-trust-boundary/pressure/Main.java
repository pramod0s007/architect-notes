import java.util.ArrayList;
import java.util.List;

public class Main {
    // Naive lookup: the user's input is concatenated directly into a WHERE
    // clause string, which is then parsed as query syntax. Nothing separates
    // "this is data the user typed" from "this is part of the query."
    static List<Order> lookupByOrderId(List<Order> orders, String userInput) {
        String whereClause = "orderId='" + userInput + "'";
        return QueryEngine.select(orders, whereClause);
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
        for (Order o : attacked) {
            System.out.println("  " + o.orderId + ": " + o.contents);
        }

        System.out.println();
        System.out.println(attacked.size() == orders.size()
            ? "CONFIRMED FAILURE: a lookup meant to return exactly one specific order, by ID, returned "
              + "every order in the system -- including two other users' confidential orders -- because "
              + "the attacker's input was concatenated into the query string and then parsed as query "
              + "syntax. The injected \"OR '1'='1'\" is not data as far as the query engine is concerned; "
              + "it's a second, always-true condition, and the engine has no way to know it wasn't "
              + "supposed to be there."
            : "Unexpected -- investigate.");
    }
}
