public final class CategorySpecification implements Specification<Product> {

    private final Product.Category required;

    public CategorySpecification(Product.Category required) {
        this.required = required;
    }

    @Override
    public boolean isSatisfiedBy(Product candidate) {
        return candidate.category() == required;
    }
}
