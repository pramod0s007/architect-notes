import java.util.HashMap;
import java.util.Map;

/**
 * The backend datastore. Tracks how many times it's actually been queried,
 * so the demo can measure backend load directly rather than inferring it.
 * A key with no record returns null, exactly like a real lookup for a
 * user, order, or object that doesn't exist.
 */
public class Backend {
    private final Map<String, String> records = new HashMap<>();
    private int lookupCount = 0;

    public String lookup(String key) {
        lookupCount++;
        return records.get(key);
    }

    public void create(String key, String value) {
        records.put(key, value);
    }

    public int lookupCount() {
        return lookupCount;
    }
}
