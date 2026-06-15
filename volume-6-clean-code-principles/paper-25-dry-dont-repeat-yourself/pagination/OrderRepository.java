import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Simulates an orders table with 9 rows. Same PageRequest/PageResponse contract.
 */
public class OrderRepository {

    private static final List<String> ALL_ORDERS = Arrays.asList(
        "ORD-1001", "ORD-1002", "ORD-1003", "ORD-1004", "ORD-1005",
        "ORD-1006", "ORD-1007", "ORD-1008", "ORD-1009"
    );

    public PageResponse<String> findAll(PageRequest req) {
        int from = Math.min(req.getOffset(), ALL_ORDERS.size());
        int to   = Math.min(from + req.getSize(), ALL_ORDERS.size());
        List<String> slice = new ArrayList<>(ALL_ORDERS.subList(from, to));
        return PageResponse.of(slice, req, ALL_ORDERS.size());
    }
}
