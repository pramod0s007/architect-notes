import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Visitor that extracts a flat metadata map from each document.
 *
 * Each document type contributes different keys; callers read the map
 * uniformly without instanceof checks.
 */
public final class MetadataExtractor implements DocumentVisitor {

    private final Map<String, String> metadata = new LinkedHashMap<>();

    @Override
    public void visit(PdfDocument pdf) {
        metadata.put("type", "PDF");
        metadata.put("file", pdf.getFileName());
        metadata.put("pages", String.valueOf(pdf.getPageCount()));
        metadata.put("passwordProtected", String.valueOf(pdf.isPasswordProtected()));
    }

    @Override
    public void visit(WordDocument word) {
        metadata.put("type", "Word");
        metadata.put("file", word.getFileName());
        metadata.put("wordCount", String.valueOf(word.getWordCount()));
        metadata.put("hasTrackChanges", String.valueOf(word.hasTrackChanges()));
    }

    @Override
    public void visit(HtmlDocument html) {
        metadata.put("type", "HTML");
        metadata.put("file", html.getFileName());
        metadata.put("encoding", html.getEncoding());
        metadata.put("hasScripts", String.valueOf(html.hasScripts()));
    }

    /** Returns the extracted metadata. Call after visiting exactly one document. */
    public Map<String, String> getMetadata() {
        return Collections.unmodifiableMap(metadata);
    }
}
