/**
 * Concrete element: an HTML document.
 */
public final class HtmlDocument implements Document {

    private final String fileName;
    private final boolean hasScripts;
    private final String encoding;

    public HtmlDocument(String fileName, boolean hasScripts, String encoding) {
        this.fileName = fileName;
        this.hasScripts = hasScripts;
        this.encoding = encoding;
    }

    @Override
    public void accept(DocumentVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    public boolean hasScripts() {
        return hasScripts;
    }

    public String getEncoding() {
        return encoding;
    }
}
