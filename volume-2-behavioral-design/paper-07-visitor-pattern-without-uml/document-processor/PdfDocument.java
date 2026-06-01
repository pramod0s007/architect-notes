/**
 * Concrete element: a PDF file.
 * Carries PDF-specific state; the visitor extracts meaning from it.
 */
public final class PdfDocument implements Document {

    private final String fileName;
    private final int pageCount;
    private final boolean passwordProtected;

    public PdfDocument(String fileName, int pageCount, boolean passwordProtected) {
        this.fileName = fileName;
        this.pageCount = pageCount;
        this.passwordProtected = passwordProtected;
    }

    @Override
    public void accept(DocumentVisitor visitor) {
        visitor.visit(this);          // dispatch to the PDF-specific overload
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    public int getPageCount() {
        return pageCount;
    }

    public boolean isPasswordProtected() {
        return passwordProtected;
    }
}
