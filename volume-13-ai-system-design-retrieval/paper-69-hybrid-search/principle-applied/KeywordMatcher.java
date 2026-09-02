import java.util.regex.Matcher;
import java.util.regex.Pattern;

class KeywordMatcher {
    // True only if EVERY term in the query appears at least once as a whole
    // word in the text -- not "some terms," all of them. This is what "the
    // user explicitly typed this" should mean.
    static boolean containsAllTerms(String query, String text) {
        for (String term : query.split("\\s+")) {
            Pattern p = Pattern.compile("\\b" + Pattern.quote(term) + "\\b", Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(text);
            if (!m.find()) {
                return false;
            }
        }
        return true;
    }
}
