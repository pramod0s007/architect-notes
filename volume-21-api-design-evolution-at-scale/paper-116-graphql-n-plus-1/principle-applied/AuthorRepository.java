import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Same simulated data-access layer as the other scenarios, plus one
 * addition: findByIds() fetches an entire batch of authors in a single
 * round trip, no matter how many distinct IDs are requested. This is what
 * a real batched SQL "WHERE id IN (...)" query (or an equivalent batched
 * datastore call) costs: one round trip, regardless of batch size.
 */
public class AuthorRepository {
    private final Map<String, Author> authorsById = new HashMap<>();
    private int callCount = 0;

    public AuthorRepository(List<Author> authors) {
        for (Author author : authors) {
            authorsById.put(author.id, author);
        }
    }

    /** One simulated round trip to the database per call. */
    public Author findById(String id) {
        callCount++;
        return authorsById.get(id);
    }

    /** One simulated round trip for the ENTIRE batch of IDs, however many are requested. */
    public Map<String, Author> findByIds(Set<String> ids) {
        callCount++;
        Map<String, Author> result = new HashMap<>();
        for (String id : ids) {
            result.put(id, authorsById.get(id));
        }
        return result;
    }

    public int callCount() {
        return callCount;
    }
}
