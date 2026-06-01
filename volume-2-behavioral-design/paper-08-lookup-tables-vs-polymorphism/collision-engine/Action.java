/**
 * Collision behavior for a keyed pair.
 * See: volume-2/.../paper-08-lookup-tables-vs-polymorphism
 */
public interface Action {

    void apply(Object a, Object b);

    String description();
}
