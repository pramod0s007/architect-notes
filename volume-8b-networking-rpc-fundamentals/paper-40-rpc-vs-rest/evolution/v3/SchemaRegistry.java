import java.util.*;

/**
 * Tracks which wire tags have ever been assigned and which have been retired. Registering a
 * field on a tag that was previously retired is refused — this is the guard real schema
 * systems (protobuf's own style guide) recommend enforcing by convention; here it's an
 * actual runtime check instead of a comment nobody reads.
 */
public class SchemaRegistry {
    private final Map<Integer, String> activeTags = new HashMap<>();
    private final Set<Integer> retiredTags = new HashSet<>();

    void registerField(int tag, String fieldName) {
        if (retiredTags.contains(tag)) {
            throw new IllegalStateException("Tag " + tag + " was retired and cannot be reused for '"
                + fieldName + "' — retired tags are reserved forever");
        }
        activeTags.put(tag, fieldName);
    }

    void retireField(int tag) {
        String fieldName = activeTags.remove(tag);
        retiredTags.add(tag);
        System.out.println("Retired tag " + tag + " (was '" + fieldName + "') — reserved forever, never reusable");
    }
}
