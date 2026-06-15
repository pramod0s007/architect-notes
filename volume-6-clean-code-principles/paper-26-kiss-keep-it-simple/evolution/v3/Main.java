import java.util.List;

public class Main {

    public static void main(String[] args) {
        List<Product> catalog = List.of(
            new Product("P1", "Running Shoes",   "Footwear", 89.99),
            new Product("P2", "Yoga Mat",        "Sports",   29.99),
            new Product("P3", "Trail Shoes",     "Footwear", 109.99),
            new Product("P4", "Basketball Shoes","Footwear", 129.99),
            new Product("P5", "Resistance Band", "Sports",   14.99)
        );

        ProductSearchService service = new ProductSearchService(catalog);

        System.out.println("--- Search by name: 'shoes' ---");
        service.searchByName("shoes").forEach(System.out::println);

        System.out.println("\n--- Filter by category: 'Sports' ---");
        service.filterByCategory("Sports").forEach(System.out::println);

        System.out.println("\n--- Filter by price: $20 - $100 ---");
        service.filterByPriceRange(20.0, 100.0).forEach(System.out::println);
    }
}
