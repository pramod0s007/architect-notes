package proxy.cachingrepository;

import java.util.*;
import java.util.stream.Collectors;

public class DatabaseProductRepository implements ProductRepository {

    private final Map<String, Product> store = new HashMap<>();

    public DatabaseProductRepository() {
        store.put("P1", new Product("P1", "Laptop",  "electronics", 999.99));
        store.put("P2", new Product("P2", "Mouse",   "electronics", 29.99));
        store.put("P3", new Product("P3", "Desk",    "furniture",   299.99));
    }

    @Override
    public Product findById(String id) {
        System.out.println("[DB] SELECT * FROM products WHERE id = '" + id + "'");
        simulateLatency();
        return store.get(id);
    }

    @Override
    public List<Product> findByCategory(String category) {
        System.out.println("[DB] SELECT * FROM products WHERE category = '" + category + "'");
        simulateLatency();
        return store.values().stream()
            .filter(p -> p.getCategory().equals(category))
            .collect(Collectors.toList());
    }

    @Override
    public void save(Product product) {
        System.out.println("[DB] INSERT/UPDATE product: " + product.getId());
        store.put(product.getId(), product);
    }

    private void simulateLatency() {
        try { Thread.sleep(50); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
