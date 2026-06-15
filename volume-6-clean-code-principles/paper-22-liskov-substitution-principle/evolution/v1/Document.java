// v1: Clean, minimal. Three methods, all implementable by any document type.
// No LSP pressure yet — we only have one implementation.
public interface Document {
    String getTitle();
    String getContent();
    void save();
}
