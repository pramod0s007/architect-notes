# Document Processor — Visitor Pattern

## What It Demonstrates

A document management system with three stable concrete types — `PdfDocument`, `WordDocument`,
`HtmlDocument` — processed by three independent operations: `ValidationVisitor`,
`MetadataExtractor`, and `TextIndexingVisitor`.

3 document types × 3 operations = 9 type/operation combinations, handled cleanly by 3 visitor
classes with zero `instanceof` checks in the calling code.

## The Pressure: Operations Grow Faster Than Types

Product requirements typically add new operations (export, redaction, accessibility check,
virus scan) far more often than they add new document formats.

Without Visitor, each new operation requires modifying all three document classes — violating
the Open/Closed Principle and scattering operational logic across domain objects.

With Visitor:
- Adding `AccessibilityCheckVisitor` = one new class, zero document changes.
- `PdfDocument` stays focused on PDF concerns; it has no knowledge of indexing or redaction.

## Class Diagram (ASCII)

```
<<interface>>                      <<interface>>
   Document                         DocumentVisitor
─────────────────                 ────────────────────────
 accept(DocumentVisitor)           visit(PdfDocument)
 getFileName(): String             visit(WordDocument)
       ▲                           visit(HtmlDocument)
       │ implements                       ▲
  ┌────┴────────┬────────────┐            │ implements
PdfDocument  WordDocument  HtmlDocument   │
─────────────────────────────    ┌────────┴──────────────────────┐
 pageCount        wordCount   ValidationVisitor  MetadataExtractor  TextIndexingVisitor
 passwordProtected trackChanges  ─────────────    ──────────────     ─────────────────
 getPageCount()   hasTrackChanges() errors: List   metadata: Map     documentsIndexed: int
                                 getErrors()       getMetadata()     getDocumentsIndexed()
```

## How Visitor Dispatch Works

```java
// In PdfDocument.java
public void accept(DocumentVisitor visitor) {
    visitor.visit(this);   // "this" is PdfDocument — compiler selects visit(PdfDocument)
}

// Caller — no instanceof, no casting
for (Document doc : documents) {
    doc.accept(validator);      // routes to validator.visit(PdfDocument/WordDocument/HtmlDocument)
}
```

The document object's concrete type selects the correct `visit()` overload at runtime.
The caller needs no knowledge of document types.

## Visitor State and Retrieval

Each visitor is **stateful** — it accumulates results during traversal, then exposes them
via a results accessor after the loop completes:

| Visitor              | State held                  | Retrieval method        |
|----------------------|-----------------------------|-------------------------|
| `ValidationVisitor`  | `List<String> errors`       | `getErrors()`           |
| `MetadataExtractor`  | `Map<String,String> metadata` | `getMetadata()`       |
| `TextIndexingVisitor`| `int documentsIndexed`      | `getDocumentsIndexed()` |

Create a fresh visitor instance per traversal to avoid state bleed between runs.

## How to Run

```bash
cd volume-2-behavioral-design/paper-07-visitor-pattern-without-uml/document-processor
javac *.java
java Main
```

Expected output (abridged):
```
=== Validation ===
  FAIL  confidential.pdf: PDF must have at least one page
  FAIL  confidential.pdf: PDF must not be password-protected for processing
  FAIL  draft-with-comments.docx: Word document must have track changes accepted
  FAIL  legacy-page.html: HTML document must not contain inline scripts (CSP violation)

=== Metadata Extraction ===
  {type=PDF, file=annual-report.pdf, pages=48, passwordProtected=false}
  ...

=== Text Indexing ===
  [INDEX] PDF 'annual-report.pdf' — extracting text from 48 page(s) via PDF renderer
  ...
Total documents submitted to index: 6
```

## Design Decisions

**Stateful visitors** allow batch processing: apply a single `ValidationVisitor` to a
collection and inspect all errors in one call to `getErrors()`, rather than receiving
results one document at a time.

**`MetadataExtractor` is single-use per document** because it accumulates into a flat map
without clearing between calls. The `Main` creates a new instance per document, which is
the intended usage pattern.

**No abstract base class** — `DocumentVisitor` is a pure interface. Any class can become
a visitor by implementing three methods, including anonymous classes and lambdas (where
single-method operation is enough).

**When to prefer this over Strategy:** use Visitor when the same operation needs type-specific
behavior across a heterogeneous collection; use Strategy when a single object needs
swappable algorithms.
