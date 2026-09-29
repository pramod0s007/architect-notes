import java.util.*;

/**
 * An RPC/protobuf-style wire message: fields are identified by a numbered tag, not a name.
 * The tag is the durable identity of a field on the wire — the human-readable accessor
 * method built on top of it can be renamed freely without changing what's actually sent.
 */
public class WireMessage {
    private final Map<Integer, Object> fields = new HashMap<>();

    void put(int tag, Object value) {
        fields.put(tag, value);
    }

    Object get(int tag) {
        return fields.get(tag);
    }
}
