package proxy.cachingrepository;

import java.util.List;

public interface ProductRepository {
    Product findById(String id);
    List<Product> findByCategory(String category);
    void save(Product product);
}
