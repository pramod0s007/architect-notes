/**
 * Demonstrates building SearchRequests for an e-commerce search API.
 *
 * Shows how the fluent builder handles:
 *   1. A simple keyword search with defaults
 *   2. A filtered category browse with price/rating/stock constraints
 *   3. A paginated, sorted brand search
 *   4. Validation catching an invalid price range
 */
public class Main {

    public static void main(String[] args) {
        SearchService service = new SearchService();

        // ── 1. Simple keyword search ──────────────────────────────────────
        System.out.println("=== 1. Simple Keyword Search ===");
        SearchRequest simple = new SearchRequest.Builder("wireless headphones")
                .build();
        service.search(simple);

        // ── 2. Filtered category browse ───────────────────────────────────
        System.out.println("=== 2. Filtered Category Browse ===");
        SearchRequest filtered = new SearchRequest.Builder("headphones")
                .category("Electronics > Audio")
                .minPrice(50.0)
                .maxPrice(300.0)
                .minRating(4.0)
                .inStockOnly(true)
                .sortBy("price")
                .sortOrder(SortOrder.ASC)
                .build();
        service.search(filtered);

        // ── 3. Paginated brand search ─────────────────────────────────────
        System.out.println("=== 3. Paginated Brand Search ===");
        SearchRequest paged = new SearchRequest.Builder("Sony")
                .category("Electronics")
                .inStockOnly(true)
                .sortBy("popularity")
                .sortOrder(SortOrder.DESC)
                .page(2)
                .pageSize(10)
                .build();
        service.search(paged);

        // ── 4. Validation — invalid price range ───────────────────────────
        System.out.println("=== 4. Validation Demo ===");
        try {
            new SearchRequest.Builder("laptop")
                    .minPrice(500.0)
                    .maxPrice(100.0)   // less than minPrice — should fail
                    .build();
        } catch (IllegalStateException e) {
            System.out.println("Caught expected error: " + e.getMessage());
        }

        try {
            new SearchRequest.Builder("  ")  // blank query — should fail
                    .build();
        } catch (IllegalArgumentException e) {
            System.out.println("Caught expected error: " + e.getMessage());
        }
    }
}
