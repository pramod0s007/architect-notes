/**
 * Concrete command — inserts text at a given position.
 * Undo removes exactly the inserted text.
 */
public final class InsertTextCommand implements Command {

    private final Document document;
    private final int position;
    private final String text;

    public InsertTextCommand(Document document, int position, String text) {
        this.document = document;
        this.position = position;
        this.text = text;
    }

    @Override
    public void execute() {
        document.insert(position, text);
    }

    @Override
    public void undo() {
        // Reverse: delete the characters we just inserted
        document.delete(position, text.length());
    }

    @Override
    public String describe() {
        return String.format("Insert \"%s\" at position %d", text, position);
    }
}
