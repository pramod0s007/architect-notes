import java.util.HashMap;
import java.util.Map;

// Spelling correction and synonym expansion.
// Hard-coded. Grows without limit. Never tested against real query logs.
public class QuerySuggestionEngine {

    private static final Map<String, String> CORRECTIONS = new HashMap<>();
    private static final Map<String, String> SYNONYMS    = new HashMap<>();

    static {
        CORRECTIONS.put("sheos",  "shoes");
        CORRECTIONS.put("shose",  "shoes");
        CORRECTIONS.put("shos",   "shoes");
        CORRECTIONS.put("shors",  "shoes");
        CORRECTIONS.put("jakcet", "jacket");
        CORRECTIONS.put("jacekt", "jacket");

        SYNONYMS.put("sneakers", "shoes");
        SYNONYMS.put("trainers", "shoes");
        SYNONYMS.put("coat",     "jacket");
        SYNONYMS.put("pullover", "sweater");
    }

    public String normalize(String query) {
        String lower     = query.toLowerCase().trim();
        String corrected = CORRECTIONS.getOrDefault(lower, lower);
        return SYNONYMS.getOrDefault(corrected, corrected);
    }
}
