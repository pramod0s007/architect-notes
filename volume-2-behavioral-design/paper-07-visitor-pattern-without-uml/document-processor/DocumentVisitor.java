/**
 * Visitor interface — one overload per concrete document type.
 *
 * Adding a new operation means adding a new implementation of this
 * interface, not touching any Document class.
 */
public interface DocumentVisitor {

    void visit(PdfDocument pdf);

    void visit(WordDocument word);

    void visit(HtmlDocument html);
}
