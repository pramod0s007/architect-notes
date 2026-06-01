/**
 * Element interface — every document type must accept a visitor.
 * The double-dispatch trick: caller picks the visitor; the concrete
 * document class picks the right overload via its own accept() body.
 */
public interface Document {

    /** Accept any DocumentVisitor — routes to the correct visit() overload. */
    void accept(DocumentVisitor visitor);

    /** Logical filename used for logging and indexing. */
    String getFileName();
}
