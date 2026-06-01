import java.util.List;

/**
 * Run: javac *.java && java Main
 */
public final class Main {

    public static void main(String[] args) {
        List<Product> catalog = List.of(
                new Product("Noise-Cancel Headphones", 349.99, Product.Category.ELECTRONICS, 4.6),
                new Product("USB-C Hub", 89.99, Product.Category.ELECTRONICS, 4.1),
                new Product("Design Patterns", 54.00, Product.Category.BOOKS, 4.8),
                new Product("Standing Desk", 1299.00, Product.Category.HOME, 4.2),
                new Product("4K Monitor", 899.00, Product.Category.ELECTRONICS, 3.9));

        Specification<Product> premiumElectronics =
                new PriceSpecification(300.0)
                        .and(new CategorySpecification(Product.Category.ELECTRONICS))
                        .and(new RatingSpecification(4.0));

        System.out.println("Rule: price >= 300 AND category == ELECTRONICS AND rating >= 4");
        System.out.println();

        catalog.stream()
                .filter(premiumElectronics::isSatisfiedBy)
                .forEach(product -> System.out.println("  match: " + product));
    }
}
