/**
 * DRY — pagination
 *
 * PageRequest and PageResponse are defined once. ProductRepository and
 * OrderRepository both use them. Pagination constraints and envelope
 * structure never drift apart because there is only one definition.
 */
public class Main {

    public static void main(String[] args) {
        ProductRepository products = new ProductRepository();
        OrderRepository orders     = new OrderRepository();

        PageRequest page0 = new PageRequest(0, 5);
        PageRequest page1 = new PageRequest(1, 5);

        System.out.println("=== Products — page 0, size 5 ===");
        PageResponse<String> p0 = products.findAll(page0);
        System.out.println(p0);
        p0.getContent().forEach(name -> System.out.println("  " + name));

        System.out.println();
        System.out.println("=== Products — page 1, size 5 ===");
        PageResponse<String> p1 = products.findAll(page1);
        System.out.println(p1);
        p1.getContent().forEach(name -> System.out.println("  " + name));

        System.out.println();
        System.out.println("=== Orders — page 0, size 5 ===");
        PageResponse<String> o0 = orders.findAll(page0);
        System.out.println(o0);
        o0.getContent().forEach(id -> System.out.println("  " + id));

        System.out.println();
        System.out.println("=== Orders — page 1, size 5 ===");
        PageResponse<String> o1 = orders.findAll(page1);
        System.out.println(o1);
        o1.getContent().forEach(id -> System.out.println("  " + id));

        System.out.println();
        System.out.println("=== Validation: page size > 100 throws ===");
        try {
            new PageRequest(0, 200);
        } catch (IllegalArgumentException e) {
            System.out.println("Caught: " + e.getMessage());
        }
    }
}
