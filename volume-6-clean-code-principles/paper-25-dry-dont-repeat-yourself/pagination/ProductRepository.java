import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Simulates a product table with 12 rows. Applies PageRequest offset/size.
 */
public class ProductRepository {

    private static final List<String> ALL_PRODUCTS = Arrays.asList(
        "Wireless Mouse", "Mechanical Keyboard", "USB-C Hub", "4K Monitor",
        "Laptop Stand", "Webcam HD", "Headset Pro", "Desk Lamp",
        "Cable Organizer", "Mouse Pad XL", "Screen Cleaner", "HDMI Cable"
    );

    public PageResponse<String> findAll(PageRequest req) {
        int from = Math.min(req.getOffset(), ALL_PRODUCTS.size());
        int to   = Math.min(from + req.getSize(), ALL_PRODUCTS.size());
        List<String> slice = new ArrayList<>(ALL_PRODUCTS.subList(from, to));
        return PageResponse.of(slice, req, ALL_PRODUCTS.size());
    }
}
