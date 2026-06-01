/**
 * Accepts a {@link Visitor} for double dispatch.
 * See: volume-2/.../paper-07-visitor-pattern-without-uml
 */
public interface GameObject {

    void accept(Visitor visitor);

    String label();
}
