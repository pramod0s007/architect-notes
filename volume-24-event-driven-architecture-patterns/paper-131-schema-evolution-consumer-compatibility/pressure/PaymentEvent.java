import java.util.HashMap;
import java.util.Map;

/**
 * A payment event represented as a loose, dynamically-typed field map — this is how
 * events actually travel on the wire (JSON), so a Map<String, Object> models real
 * schema flexibility rather than a fixed, compiler-checked Java class that could never
 * silently change shape the way a real wire format can.
 */
public class PaymentEvent {
    private final Map<String, Object> fields = new HashMap<>();

    public void set(String key, Object value) {
        fields.put(key, value);
    }

    public Object get(String key) {
        return fields.get(key);
    }
}
