import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// A small, faithful stand-in for how a real database evaluates a WHERE
// clause built by string concatenation. It doesn't matter that this is a
// toy -- the mechanism is the same one a real SQL engine uses: OR is
// case-insensitive (exactly like real SQL keywords), and the string is
// parsed as SYNTAX, not compared as a single opaque value.
class QueryEngine {
    private static final Pattern FIELD_EQUALS = Pattern.compile("orderId\\s*=\\s*'([^']*)'");
    private static final Pattern LITERAL_EQUALS = Pattern.compile("'([^']*)'\\s*=\\s*'([^']*)'");

    static List<Order> select(List<Order> orders, String whereClause) {
        String[] orTerms = whereClause.split("(?i)\\s+OR\\s+");
        List<Order> matches = new ArrayList<>();
        for (Order order : orders) {
            for (String term : orTerms) {
                if (termIsTrueFor(order, term.trim())) {
                    matches.add(order);
                    break; // one true OR-term is enough to match this record
                }
            }
        }
        return matches;
    }

    private static boolean termIsTrueFor(Order order, String term) {
        Matcher literal = LITERAL_EQUALS.matcher(term);
        if (literal.matches()) {
            return literal.group(1).equals(literal.group(2)); // e.g. '1'='1' -- always true
        }
        Matcher field = FIELD_EQUALS.matcher(term);
        if (field.matches()) {
            return order.orderId.equals(field.group(1));
        }
        return false;
    }
}
