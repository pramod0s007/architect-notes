/**
 * Composable business rule.
 * See: volume-3/.../paper-09-specification-pattern
 */
public interface Specification<T> {

    boolean isSatisfiedBy(T candidate);

    default Specification<T> and(Specification<T> other) {
        return new AndSpecification<>(this, other);
    }
}
