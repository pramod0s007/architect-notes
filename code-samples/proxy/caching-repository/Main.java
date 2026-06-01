package proxy.cachingrepository;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        // Caller sees ProductRepository — doesn't know it's a proxy
        ProductRepository repository = new CachingProductRepositoryProxy(
            new DatabaseProductRepository());

        System.out.println("=== First access (cache miss — hits DB) ===");
        System.out.println(repository.findById("P1"));

        System.out.println("\n=== Second access (cache hit — no DB) ===");
        System.out.println(repository.findById("P1"));

        System.out.println("\n=== Category query (cache miss) ===");
        repository.findByCategory("electronics").forEach(System.out::println);

        System.out.println("\n=== Category query again (cache hit) ===");
        repository.findByCategory("electronics").forEach(System.out::println);

        System.out.println("\n=== Update invalidates cache ===");
        Product laptop = repository.findById("P1");
        laptop.setPrice(899.99);
        repository.save(laptop);

        System.out.println("\n=== Post-update fetch (cache miss — price updated) ===");
        System.out.println(repository.findById("P1"));
    }
}
