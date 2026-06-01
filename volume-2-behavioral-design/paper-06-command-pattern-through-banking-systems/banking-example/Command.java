/**
 * Encapsulated behavior for {@link Invoker}.
 * See: volume-2/.../paper-06-command-pattern-through-banking-systems
 */
public interface Command {

    void execute();

    void undo();

    String name();
}
