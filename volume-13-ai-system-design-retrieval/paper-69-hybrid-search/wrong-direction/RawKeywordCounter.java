import java.util.regex.Matcher;
import java.util.regex.Pattern;

class RawKeywordCounter {
    // Sums how many times EACH query term occurs in the text -- raw frequency,
    // unbounded, with no normalization against the vector score's 0..1 range.
    static int totalOccurrences(String query, String text) {
        int total = 0;
        for (String term : query.split("\\s+")) {
            Pattern p = Pattern.compile("\\b" + Pattern.quote(term) + "\\b", Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(text);
            while (m.find()) {
                total++;
            }
        }
        return total;
    }
}
