import java.util.*;

/**
 * A REST/JSON-style wire message: fields are identified by their string key. This is exactly
 * how a parsed JSON object behaves — {@code get("totalPrice")} only works if the sender used
 * the string "totalPrice" as the key.
 */
public class JsonMessage {
    private final Map<String, Object> fields = new HashMap<>();

    void put(String key, Object value) {
        fields.put(key, value);
    }

    Object get(String key) {
        return fields.get(key); // returns null if the key isn't present — no error, no signal
    }
}
