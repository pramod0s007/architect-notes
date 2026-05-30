public final class RatingSpecification implements Specification<Product> {

    private final double minimumRating;

    public RatingSpecification(double minimumRating) {
        this.minimumRating = minimumRating;
    }

    @Override
    public boolean isSatisfiedBy(Product candidate) {
        return candidate.rating() >= minimumRating;
    }
}
