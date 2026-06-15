import java.util.ArrayList;
import java.util.List;

// Handles 100% of actual use cases with three focused, testable methods.
// When full-text search becomes a measured requirement,
// Elasticsearch replaces this class — not the callers.
public class ProductSearchService {

    private final List<Product> catalog;

    public ProductSearchService(List<Product> catalog) {
        this.catalog = catalog;
    }

    public List<Product> searchByName(String query) {
        List<Product> results = new ArrayList<>();
        String lower = query.toLowerCase();
        for (Product p : catalog) {
            if (p.name.toLowerCase().contains(lower)) {
                results.add(p);
            }
        }
        return results;
    }

    public List<Product> filterByCategory(String category) {
        List<Product> results = new ArrayList<>();
        for (Product p : catalog) {
            if (p.category.equalsIgnoreCase(category)) {
                results.add(p);
            }
        }
        return results;
    }

    public List<Product> filterByPriceRange(double minPrice, double maxPrice) {
        List<Product> results = new ArrayList<>();
        for (Product p : catalog) {
            if (p.price >= minPrice && p.price <= maxPrice) {
                results.add(p);
            }
        }
        return results;
    }
}
