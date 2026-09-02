import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Identical query engine to the pressure case -- OR splitting is
// case-insensitive, exactly like real SQL keywords. The overcorrection
// doesn't touch this engine at all; it tries to fix the problem entirely on
// the input side, before concatenation.
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
                    break;
                }
            }
        }
        return matches;
    }

    private static boolean termIsTrueFor(Order order, String term) {
        Matcher literal = LITERAL_EQUALS.matcher(term);
        if (literal.matches()) {
            return literal.group(1).equals(literal.group(2));
        }
        Matcher field = FIELD_EQUALS.matcher(term);
        if (field.matches()) {
            return order.orderId.equals(field.group(1));
        }
        return false;
    }
}
