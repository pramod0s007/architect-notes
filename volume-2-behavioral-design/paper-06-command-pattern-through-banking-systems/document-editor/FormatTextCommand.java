/**
 * Concrete command — applies a formatting tag (bold, italic, underline) to a range.
 * Undo removes the formatting, restoring the prior state.
 */
public final class FormatTextCommand implements Command {

    private final Document document;
    private final int start;
    private final int end;
    private final String format;

    public FormatTextCommand(Document document, int start, int end, String format) {
        this.document = document;
        this.start = start;
        this.end = end;
        this.format = format;
    }

    @Override
    public void execute() {
        document.applyFormat(start, end, format);
    }

    @Override
    public void undo() {
        document.removeFormat(start, end);
    }

    @Override
    public String describe() {
        return String.format("Format [%d-%d] as %s", start, end, format);
    }
}
