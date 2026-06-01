/**
 * Command interface — every edit operation must be executable and undoable.
 * This is what enables the undo/redo stack in {@link EditorHistory}.
 */
public interface Command {

    /** Apply the operation to the document. */
    void execute();

    /** Reverse the operation exactly as it was applied. */
    void undo();

    /** Human-readable description for history log display. */
    String describe();
}
