import java.util.List;

/**
 * Run: javac *.java && java Main
 *
 * Demonstrates three independent operations applied to the same document
 * collection without any instanceof checks.
 */
public final class Main {

    public static void main(String[] args) {

        List<Document> documents = List.of(
                new PdfDocument("annual-report.pdf", 48, false),
                new PdfDocument("confidential.pdf", 0, true),      // will fail validation
                new WordDocument("project-proposal.docx", 3200, false),
                new WordDocument("draft-with-comments.docx", 1500, true), // will fail validation
                new HtmlDocument("landing-page.html", false, "UTF-8"),
                new HtmlDocument("legacy-page.html", true, "ISO-8859-1")  // will fail validation
        );

        // ── 1. Validation ────────────────────────────────────────────────────
        System.out.println("=== Validation ===");
        ValidationVisitor validator = new ValidationVisitor();
        for (Document doc : documents) {
            doc.accept(validator);
        }
        if (validator.isValid()) {
            System.out.println("  All documents passed validation.");
        } else {
            validator.getErrors().forEach(e -> System.out.println("  FAIL  " + e));
        }

        // ── 2. Metadata extraction ────────────────────────────────────────────
        System.out.println("\n=== Metadata Extraction ===");
        for (Document doc : documents) {
            MetadataExtractor extractor = new MetadataExtractor();
            doc.accept(extractor);
            System.out.println("  " + extractor.getMetadata());
        }

        // ── 3. Text indexing ─────────────────────────────────────────────────
        System.out.println("\n=== Text Indexing ===");
        TextIndexingVisitor indexer = new TextIndexingVisitor();
        for (Document doc : documents) {
            doc.accept(indexer);
        }
        System.out.printf("%nTotal documents submitted to index: %d%n",
                indexer.getDocumentsIndexed());
    }
}
