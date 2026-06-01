import java.util.ArrayList;
import java.util.List;

/**
 * Simulates an e-commerce search engine. Accepts a {@link SearchRequest} and
 * prints which filters it would apply. In production this would translate the
 * request to an Elasticsearch or Solr query.
 */
public class SearchService {

    public void search(SearchRequest request) {
        System.out.println("  Executing search: " + request);

        List<String> filters = new ArrayList<>();
        filters.add("query='" + request.getQuery() + "'");

        if (request.hasCategory()) {
            filters.add("category='" + request.getCategory() + "'");
        }
        if (request.hasPriceFilter()) {
            String max = request.getMaxPrice() == Double.MAX_VALUE ? "∞" : String.valueOf(request.getMaxPrice());
            filters.add("price=[" + request.getMinPrice() + ".." + max + "]");
        }
        if (request.hasRatingFilter()) {
            filters.add("rating>=" + request.getMinRating());
        }
        if (request.isInStockOnly()) {
            filters.add("inStock=true");
        }

        System.out.println("  Active filters : " + filters);
        System.out.println("  Sort           : " + request.getSortBy() + " " + request.getSortOrder());
        System.out.println("  Page           : " + request.getPage() + " (size=" + request.getPageSize() + ")");
        System.out.println("  -> Returning simulated results...");
        System.out.println();
    }
}
