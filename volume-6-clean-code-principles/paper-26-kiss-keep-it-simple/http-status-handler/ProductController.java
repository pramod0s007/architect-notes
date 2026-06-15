import java.util.Arrays;
import java.util.List;

/**
 * KISS: uses the simple ApiResponse — each method is immediately readable.
 */
public class ProductController {

    private static final List<String> CATALOGUE = Arrays.asList(
        "Wireless Mouse", "Mechanical Keyboard", "USB-C Hub"
    );

    public ApiResponse getProduct(String id) {
        int index;
        try {
            index = Integer.parseInt(id);
        } catch (NumberFormatException e) {
            return ApiResponse.badRequest("Product id must be numeric: " + id);
        }
        if (index < 0 || index >= CATALOGUE.size()) {
            return ApiResponse.notFound("Product not found: " + id);
        }
        return ApiResponse.ok(CATALOGUE.get(index));
    }

    public ApiResponse listProducts() {
        return ApiResponse.ok(CATALOGUE);
    }
}
