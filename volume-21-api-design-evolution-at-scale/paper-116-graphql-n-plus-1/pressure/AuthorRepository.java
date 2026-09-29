import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simulates a data-access layer backed by a real database. Every call to
 * findById() counts as one round trip -- exactly what a GraphQL resolver's
 * per-field fetch would cost against a real datastore.
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

    public int callCount() {
        return callCount;
    }
}
