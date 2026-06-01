package proxy.cachingrepository;

import java.util.*;

public class CachingProductRepositoryProxy implements ProductRepository {

    private final ProductRepository real;
    private final Map<String, Product> idCache = new HashMap<>();
    private final Map<String, List<Product>> categoryCache = new HashMap<>();

    public CachingProductRepositoryProxy(ProductRepository real) {
        this.real = real;
    }

    @Override
    public Product findById(String id) {
        if (idCache.containsKey(id)) {
            System.out.println("[CACHE HIT] findById: " + id);
            return idCache.get(id);
        }
        System.out.println("[CACHE MISS] findById: " + id);
        Product product = real.findById(id);
        if (product != null) idCache.put(id, product);
        return product;
    }

    @Override
    public List<Product> findByCategory(String category) {
        if (categoryCache.containsKey(category)) {
            System.out.println("[CACHE HIT] findByCategory: " + category);
            return categoryCache.get(category);
        }
        System.out.println("[CACHE MISS] findByCategory: " + category);
        List<Product> products = real.findByCategory(category);
        categoryCache.put(category, products);
        return products;
    }

    @Override
    public void save(Product product) {
        // Invalidate cache on write
        idCache.remove(product.getId());
        categoryCache.remove(product.getCategory());
        System.out.println("[CACHE] Invalidated entries for: " + product.getId());
        real.save(product);
    }
}
