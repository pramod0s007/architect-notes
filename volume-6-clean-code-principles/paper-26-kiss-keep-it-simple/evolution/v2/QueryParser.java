import java.util.ArrayList;
import java.util.List;

// Parses AND/OR/NOT operators, phrase queries, field-specific searches.
// 80+ lines solving a problem no user has reported needing.
public class QueryParser {

    public static class Token {
        public final String type;  // WORD, AND, OR, NOT, PHRASE, FIELD
        public final String value;
        public Token(String type, String value) { this.type = type; this.value = value; }
    }

    public List<Token> parse(String query) {
        List<Token> tokens = new ArrayList<>();
        String[] parts = query.split("\\s+");
        int i = 0;
        while (i < parts.length) {
            String part = parts[i];
            if (part.equalsIgnoreCase("AND")) {
                tokens.add(new Token("AND", "AND"));
            } else if (part.equalsIgnoreCase("OR")) {
                tokens.add(new Token("OR", "OR"));
            } else if (part.equalsIgnoreCase("NOT")) {
                tokens.add(new Token("NOT", "NOT"));
            } else if (part.startsWith("\"")) {
                StringBuilder phrase = new StringBuilder(part.substring(1));
                while (i + 1 < parts.length && !parts[i].endsWith("\"")) {
                    i++;
                    phrase.append(" ").append(parts[i]);
                }
                String p = phrase.toString().replace("\"", "");
                tokens.add(new Token("PHRASE", p));
            } else if (part.contains(":")) {
                String[] kv = part.split(":", 2);
                tokens.add(new Token("FIELD:" + kv[0], kv[1]));
            } else {
                tokens.add(new Token("WORD", part));
            }
            i++;
        }
        return tokens;
    }

    public boolean matches(Product product, List<Token> tokens) {
        boolean result = false;
        String pending = "OR";
        for (Token t : tokens) {
            if (t.type.equals("AND") || t.type.equals("OR")) {
                pending = t.type;
                continue;
            }
            boolean match = termMatches(product, t);
            if (pending.equals("AND")) result = result && match;
            else                       result = result || match;
            pending = "OR";
        }
        return result;
    }

    private boolean termMatches(Product product, Token t) {
        if (t.type.equals("NOT"))          return false;
        if (t.type.startsWith("FIELD:")) {
            String field = t.type.substring(6);
            if (field.equals("category")) return product.category.equalsIgnoreCase(t.value);
            if (field.equals("name"))     return product.name.toLowerCase().contains(t.value.toLowerCase());
        }
        String v = t.value.toLowerCase();
        return product.name.toLowerCase().contains(v)
            || product.category.toLowerCase().contains(v);
    }
}
