import java.util.ArrayDeque;
import java.util.Deque;

public final class Invoker {

    private final Deque<Command> history = new ArrayDeque<>();

    public void run(Command command) {
        command.execute();
        history.push(command);
    }

    public void undo() {
        if (history.isEmpty()) {
            throw new IllegalStateException("Nothing to undo");
        }
        history.pop().undo();
    }

    public boolean canUndo() {
        return !history.isEmpty();
    }
}
