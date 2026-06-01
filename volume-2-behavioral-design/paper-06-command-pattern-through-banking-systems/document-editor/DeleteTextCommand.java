/**
 * Concrete command — deletes a range of characters.
 * Undo re-inserts the deleted text at the same position.
 * The deleted text is captured at execute() time so undo is always accurate.
 */
public final class DeleteTextCommand implements Command {

    private final Document document;
    private final int position;
    private final int length;

    /** Captured when execute() runs — needed for accurate undo. */
    private String deletedText;

    public DeleteTextCommand(Document document, int position, int length) {
        this.document = document;
        this.position = position;
        this.length = length;
    }

    @Override
    public void execute() {
        // Snapshot the text before deleting so undo can restore it precisely
        deletedText = document.getContent().substring(position, position + length);
        document.delete(position, length);
    }

    @Override
    public void undo() {
        if (deletedText == null) {
            throw new IllegalStateException("DeleteTextCommand has not been executed yet");
        }
        document.insert(position, deletedText);
    }

    @Override
    public String describe() {
        String preview = deletedText != null ? "\"" + deletedText + "\"" : length + " chars";
        return String.format("Delete %s at position %d", preview, position);
    }
}
