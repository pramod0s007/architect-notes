public final class PriceSpecification implements Specification<Product> {

    private final double minimumPrice;

    public PriceSpecification(double minimumPrice) {
        this.minimumPrice = minimumPrice;
    }

    @Override
    public boolean isSatisfiedBy(Product candidate) {
        return candidate.price() >= minimumPrice;
    }
}
