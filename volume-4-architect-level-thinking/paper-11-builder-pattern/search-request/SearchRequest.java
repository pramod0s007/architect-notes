/**
 * Immutable value object representing an e-commerce product search request.
 *
 * All parameters except {@code query} are optional; the Builder provides
 * sensible defaults and validates that min/max price and rating constraints
 * are internally consistent.
 */
public final class SearchRequest {

    private final String  query;
    private final String  category;
    private final double  minPrice;
    private final double  maxPrice;
    private final double  minRating;
    private final boolean inStockOnly;
    private final int     page;
    private final int     pageSize;
    private final String  sortBy;
    private final SortOrder sortOrder;

    private SearchRequest(Builder b) {
        this.query       = b.query;
        this.category    = b.category;
        this.minPrice    = b.minPrice;
        this.maxPrice    = b.maxPrice;
        this.minRating   = b.minRating;
        this.inStockOnly = b.inStockOnly;
        this.page        = b.page;
        this.pageSize    = b.pageSize;
        this.sortBy      = b.sortBy;
        this.sortOrder   = b.sortOrder;
    }

    // ── Accessors ────────────────────────────────────────────────────────────

    public String   getQuery()       { return query; }
    public String   getCategory()    { return category; }
    public double   getMinPrice()    { return minPrice; }
    public double   getMaxPrice()    { return maxPrice; }
    public double   getMinRating()   { return minRating; }
    public boolean  isInStockOnly()  { return inStockOnly; }
    public int      getPage()        { return page; }
    public int      getPageSize()    { return pageSize; }
    public String   getSortBy()      { return sortBy; }
    public SortOrder getSortOrder()  { return sortOrder; }

    public boolean hasCategory()     { return category != null && !category.isBlank(); }
    public boolean hasPriceFilter()  { return minPrice > 0 || maxPrice < Double.MAX_VALUE; }
    public boolean hasRatingFilter() { return minRating > 0; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("SearchRequest{");
        sb.append("query='").append(query).append('\'');
        if (hasCategory())     sb.append(", category='").append(category).append('\'');
        if (hasPriceFilter())  sb.append(", price=[").append(minPrice).append("..").append(maxPrice == Double.MAX_VALUE ? "∞" : maxPrice).append("]");
        if (hasRatingFilter()) sb.append(", minRating=").append(minRating);
        if (inStockOnly)       sb.append(", inStockOnly=true");
        sb.append(", page=").append(page);
        sb.append(", pageSize=").append(pageSize);
        sb.append(", sortBy='").append(sortBy).append("'");
        sb.append(", sortOrder=").append(sortOrder);
        sb.append('}');
        return sb.toString();
    }

    // ── Builder ──────────────────────────────────────────────────────────────

    public static class Builder {

        // Required
        private final String query;

        // Optional — defaults
        private String   category    = null;
        private double   minPrice    = 0.0;
        private double   maxPrice    = Double.MAX_VALUE;
        private double   minRating   = 0.0;
        private boolean  inStockOnly = false;
        private int      page        = 0;
        private int      pageSize    = 20;
        private String   sortBy      = "relevance";
        private SortOrder sortOrder  = SortOrder.DESC;

        public Builder(String query) {
            if (query == null || query.isBlank()) {
                throw new IllegalArgumentException("Search query cannot be blank");
            }
            this.query = query;
        }

        public Builder category(String category)    { this.category    = category; return this; }
        public Builder minPrice(double minPrice)    { this.minPrice    = minPrice; return this; }
        public Builder maxPrice(double maxPrice)    { this.maxPrice    = maxPrice; return this; }
        public Builder minRating(double minRating)  { this.minRating   = minRating; return this; }
        public Builder inStockOnly(boolean b)       { this.inStockOnly = b;        return this; }
        public Builder page(int page)               { this.page        = page;     return this; }
        public Builder pageSize(int pageSize)       { this.pageSize    = pageSize; return this; }
        public Builder sortBy(String sortBy)        { this.sortBy      = sortBy;   return this; }
        public Builder sortOrder(SortOrder order)   { this.sortOrder   = order;    return this; }

        public SearchRequest build() {
            validate();
            return new SearchRequest(this);
        }

        private void validate() {
            if (minPrice < 0) {
                throw new IllegalStateException("minPrice cannot be negative");
            }
            if (maxPrice < minPrice) {
                throw new IllegalStateException(
                    "maxPrice (" + maxPrice + ") must be >= minPrice (" + minPrice + ")");
            }
            if (minRating < 0 || minRating > 5) {
                throw new IllegalStateException("minRating must be between 0 and 5");
            }
            if (page < 0) {
                throw new IllegalStateException("page cannot be negative");
            }
            if (pageSize < 1 || pageSize > 100) {
                throw new IllegalStateException("pageSize must be between 1 and 100");
            }
        }
    }
}
