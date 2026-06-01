import java.util.HashMap;
import java.util.Map;

/**
 * Simple mutable text document.
 * Tracks content as a StringBuilder and a per-range formatting map.
 * This is the Receiver — it knows how to carry out the actual edits.
 */
public final class Document {

    private final StringBuilder content = new StringBuilder();

    /** Stores active formatting tags for a region, keyed by "start:end". */
    private final Map<String, String> formatting = new HashMap<>();

    // --- Core edit operations (called by Command objects) ---

    public void insert(int position, String text) {
        if (position < 0 || position > content.length()) {
            throw new IllegalArgumentException("Insert position out of range: " + position);
        }
        content.insert(position, text);
    }

    public void delete(int position, int length) {
        if (position < 0 || position + length > content.length()) {
            throw new IllegalArgumentException("Delete range out of bounds: pos=" + position + " len=" + length);
        }
        content.delete(position, position + length);
    }

    public void applyFormat(int start, int end, String format) {
        formatting.put(start + ":" + end, format);
    }

    public void removeFormat(int start, int end) {
        formatting.remove(start + ":" + end);
    }

    public String getContent() {
        return content.toString();
    }

    public int length() {
        return content.length();
    }

    public String getActiveFormats() {
        if (formatting.isEmpty()) return "(none)";
        return formatting.toString();
    }
}
