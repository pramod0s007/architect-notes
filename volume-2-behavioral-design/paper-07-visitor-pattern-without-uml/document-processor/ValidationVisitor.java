import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Visitor that validates documents according to type-specific rules.
 *
 * PDF  — must have at least one page and must not be password-protected.
 * Word — must not have unresolved track changes.
 * HTML — must not contain inline scripts (content security policy).
 *
 * Collect all errors first, then inspect via {@link #getErrors()}.
 */
public final class ValidationVisitor implements DocumentVisitor {

    private final List<String> errors = new ArrayList<>();

    @Override
    public void visit(PdfDocument pdf) {
        if (pdf.getPageCount() <= 0) {
            errors.add(pdf.getFileName() + ": PDF must have at least one page");
        }
        if (pdf.isPasswordProtected()) {
            errors.add(pdf.getFileName() + ": PDF must not be password-protected for processing");
        }
    }

    @Override
    public void visit(WordDocument word) {
        if (word.hasTrackChanges()) {
            errors.add(word.getFileName() + ": Word document must have track changes accepted before submission");
        }
    }

    @Override
    public void visit(HtmlDocument html) {
        if (html.hasScripts()) {
            errors.add(html.getFileName() + ": HTML document must not contain inline scripts (CSP violation)");
        }
    }

    /** Returns an unmodifiable view of all validation errors collected so far. */
    public List<String> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    public boolean isValid() {
        return errors.isEmpty();
    }
}
