import java.util.ArrayList;
import java.util.List;

// Works for basic case. Simple and direct.
// Misses requirement: no category filter, no price range.
public class ProductSearch {

    private final List<Product> catalog;

    public ProductSearch(List<Product> catalog) {
        this.catalog = catalog;
    }

    public List<Product> search(String query) {
        List<Product> results = new ArrayList<>();
        String lower = query.toLowerCase();
        for (Product p : catalog) {
            if (p.name.toLowerCase().contains(lower)) {
                results.add(p);
            }
        }
        return results;
    }

    public static void main(String[] args) {
        List<Product> catalog = List.of(
            new Product("P1", "Running Shoes", "Footwear", 89.99),
            new Product("P2", "Yoga Mat",      "Sports",   29.99),
            new Product("P3", "Trail Shoes",   "Footwear", 109.99)
        );

        ProductSearch search = new ProductSearch(catalog);
        search.search("shoes").forEach(System.out::println);
    }
}
