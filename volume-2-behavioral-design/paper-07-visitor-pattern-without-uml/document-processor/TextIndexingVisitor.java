/**
 * Visitor that simulates submitting each document's text to a search index.
 *
 * Real implementation would call an indexing API (Elasticsearch, Solr, etc.).
 * The simulation prints what would be indexed, demonstrating that each document
 * type requires a different extraction strategy — all hidden behind visit().
 */
public final class TextIndexingVisitor implements DocumentVisitor {

    private int documentsIndexed = 0;

    @Override
    public void visit(PdfDocument pdf) {
        // PDFs: extract text page by page
        System.out.printf("  [INDEX] PDF '%s' — extracting text from %d page(s) via PDF renderer%n",
                pdf.getFileName(), pdf.getPageCount());
        documentsIndexed++;
    }

    @Override
    public void visit(WordDocument word) {
        // Word docs: parse OOXML content stream
        System.out.printf("  [INDEX] Word '%s' — indexing %d words from OOXML content stream%n",
                word.getFileName(), word.getWordCount());
        documentsIndexed++;
    }

    @Override
    public void visit(HtmlDocument html) {
        // HTML: strip tags, honour charset encoding
        System.out.printf("  [INDEX] HTML '%s' — stripping tags, encoding=%s%n",
                html.getFileName(), html.getEncoding());
        documentsIndexed++;
    }

    public int getDocumentsIndexed() {
        return documentsIndexed;
    }
}
