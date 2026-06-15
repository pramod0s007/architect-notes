/**
 * KISS — http-status-handler
 *
 * The simple ApiResponse handles every real case without generics,
 * builders, or metadata abstractions. Over-engineered version shown
 * for contrast — complexity with no tangible benefit.
 */
public class Main {

    public static void main(String[] args) {
        ProductController controller = new ProductController();

        System.out.println("=== Simple ApiResponse — all real cases ===");
        System.out.println(controller.listProducts());
        System.out.println(controller.getProduct("0"));
        System.out.println(controller.getProduct("99"));
        System.out.println(controller.getProduct("abc"));

        System.out.println();
        System.out.println("=== Over-engineered alternative (same result, more noise) ===");
        OverEngineeredApiResponse<String, ApiError, ResponseMetadata> response =
                OverEngineeredApiResponse.success("Wireless Mouse");
        System.out.println(response);
        System.out.println("Caller needed 3 type parameters for a simple success response.");
        System.out.println("SimpleApiResponse needed: ApiResponse.ok(\"Wireless Mouse\")");
    }
}
