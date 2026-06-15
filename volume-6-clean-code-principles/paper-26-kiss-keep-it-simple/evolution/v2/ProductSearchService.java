import java.util.ArrayList;
import java.util.List;

// KISS violation: 3 months of work for what Elasticsearch's built-in match query
// handles in 2 days. AbTestingFramework is instantiated but never used.
public class ProductSearchService {

    private final List<Product>          catalog;
    private final QueryParser            parser;
    private final RelevanceScorer        scorer;
    private final QuerySuggestionEngine  suggestions;
    private final AbTestingFramework     abTesting; // never used

    public ProductSearchService(List<Product> catalog) {
        this.catalog     = catalog;
        this.parser      = new QueryParser();
        this.scorer      = new RelevanceScorer(2.0, 1.0, 1.5, 0.5, 0.8);
        this.suggestions = new QuerySuggestionEngine();
        this.abTesting   = new AbTestingFramework(); // wired in, never called
    }

    public List<Product> search(String rawQuery) {
        String normalized             = suggestions.normalize(rawQuery);
        List<QueryParser.Token> tokens = parser.parse(normalized);

        List<Product> matched = new ArrayList<>();
        for (Product p : catalog) {
            if (parser.matches(p, tokens)) {
                matched.add(p);
            }
        }

        matched.sort((a, b) ->
            Double.compare(scorer.score(b, normalized), scorer.score(a, normalized))
        );

        return matched;
    }
}
