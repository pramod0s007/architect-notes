/**
 * Composable business rule.
 *
 * Default methods enable fluent composition:
 * <pre>
 *   Specification&lt;Customer&gt; eligible =
 *       minAge.and(minIncome).and(minCredit).and(notBlacklisted);
 * </pre>
 */
public interface Specification<T> {

    boolean isSatisfiedBy(T candidate);

    default Specification<T> and(Specification<T> other) {
        return candidate -> this.isSatisfiedBy(candidate) && other.isSatisfiedBy(candidate);
    }

    default Specification<T> or(Specification<T> other) {
        return candidate -> this.isSatisfiedBy(candidate) || other.isSatisfiedBy(candidate);
    }

    default Specification<T> not() {
        return candidate -> !this.isSatisfiedBy(candidate);
    }
}
