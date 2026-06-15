import java.util.HashMap;
import java.util.Map;

// Holds experiment config and variant selection.
// Never actually wired up to anything. Built "just in case."
public class AbTestingFramework {

    private final Map<String, Object> experimentConfig = new HashMap<>();

    public void configure(String key, Object value) {
        experimentConfig.put(key, value);
    }

    public String selectVariant(String userId, String experiment) {
        // Stub — variant selection logic never implemented.
        // userId ignored. Always returns "control."
        return "control";
    }

    public Object getConfig(String key) {
        return experimentConfig.get(key);
    }
}
