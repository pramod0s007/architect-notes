import java.util.ArrayList;
import java.util.List;

public class Main {
    // Overcorrection: still concatenates user input into a query string --
    // that part of the pressure case's mistake is untouched. Instead, it
    // tries to defuse the danger by stripping the exact, case-sensitive
    // substring "OR" out of the input before building the query.
    static String sanitize(String input) {
        return input.replace("OR", "");
    }

    static List<Order> lookupByOrderId(List<Order> orders, String userInput) {
        String sanitized = sanitize(userInput);
        String whereClause = "orderId='" + sanitized + "'";
        return QueryEngine.select(orders, whereClause);
    }

    public static void main(String[] args) {
        List<Order> orders = new ArrayList<>();
        orders.add(new Order("O100", "U1", "Alice's prescription order"));
        orders.add(new Order("O200", "U2", "Bob's confidential order"));
        orders.add(new Order("O300", "U3", "Carol's confidential order"));

        String originalAttack = "nonexistent' OR '1'='1";
        System.out.println("Original attack input: " + originalAttack);
        List<Order> blocked = lookupByOrderId(orders, originalAttack);
        System.out.println("Records returned: " + blocked.size() + " (the uppercase OR was stripped, this specific payload is defused)");

        System.out.println();
        String mixedCaseAttack = "nonexistent' oR '1'='1";
        System.out.println("Mixed-case variant: " + mixedCaseAttack);
        List<Order> bypassed = lookupByOrderId(orders, mixedCaseAttack);
        System.out.println("Records returned: " + bypassed.size());
        for (Order o : bypassed) {
            System.out.println("  " + o.orderId + ": " + o.contents);
        }

        System.out.println();
        System.out.println(bypassed.size() == orders.size()
            ? "CONFIRMED FAILURE: the exact same injection succeeds again, just by changing the case of "
              + "one word. The denylist strips the literal substring \"OR\", but the query engine parses "
              + "OR case-insensitively, exactly like a real SQL database does -- \"oR\" is never touched "
              + "by the filter and is still a fully valid OR to the engine underneath. The concatenation "
              + "into a parsed query string was never fixed; only one specific spelling of one specific "
              + "attack was blocked."
            : "Unexpected -- investigate.");
    }
}
