import java.util.Map;

/**
 * Abstraction for a business report. Implementations may be expensive to
 * fully materialise (PDF generation, large database queries). The proxy
 * implements this interface to defer that cost.
 */
public interface Report {

    /**
     * Returns the report title. Always cheap — no heavy computation.
     */
    String getTitle();

    /**
     * Returns the rendered PDF content as raw bytes.
     * This may be an expensive operation (rendering, signing, compression).
     */
    byte[] getPdfContent();

    /**
     * Returns report metadata (author, generated-at, page count, etc.).
     * Also potentially expensive — may require hitting a metadata service.
     */
    Map<String, String> getMetadata();
}
