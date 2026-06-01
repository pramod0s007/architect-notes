import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Invoker — manages the undo and redo stacks.
 *
 * Every edit goes through {@link #execute(Command)}.
 * {@link #undo()} reverses the most recent command and pushes it to the redo stack.
 * {@link #redo()} re-applies the most recently undone command.
 * Any new execute() after an undo clears the redo stack (standard editor behaviour).
 */
public final class EditorHistory {

    private final Deque<Command> undoStack = new ArrayDeque<>();
    private final Deque<Command> redoStack = new ArrayDeque<>();

    public void execute(Command command) {
        command.execute();
        undoStack.push(command);
        redoStack.clear(); // branching edit invalidates redo history
        System.out.println("  executed: " + command.describe());
    }

    public void undo() {
        if (undoStack.isEmpty()) {
            System.out.println("  [undo] nothing to undo");
            return;
        }
        Command command = undoStack.pop();
        command.undo();
        redoStack.push(command);
        System.out.println("  undone:   " + command.describe());
    }

    public void redo() {
        if (redoStack.isEmpty()) {
            System.out.println("  [redo] nothing to redo");
            return;
        }
        Command command = redoStack.pop();
        command.execute();
        undoStack.push(command);
        System.out.println("  redone:   " + command.describe());
    }

    public boolean canUndo() { return !undoStack.isEmpty(); }
    public boolean canRedo() { return !redoStack.isEmpty(); }
    public int undoDepth()   { return undoStack.size(); }
}
