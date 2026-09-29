import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The team's fix after noticing the N+1 problem: a per-request memoization
 * cache, so the same author ID is never fetched twice within one query.
 * This is a real, correct optimization for duplicate keys -- but it does
 * nothing for distinct keys, which is exactly the shape of a typical
 * public feed, where most posts are written by different authors.
 */
public class MemoizingPostAuthorResolver {
    private final AuthorRepository authorRepository;
    private final Map<String, Author> requestCache = new HashMap<>();

    public MemoizingPostAuthorResolver(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    public void resolveAuthors(List<Post> posts) {
        for (Post post : posts) {
            Author author = requestCache.computeIfAbsent(post.authorId, authorRepository::findById);
            System.out.println("  Post '" + post.title + "' -> author: " + author);
        }
    }
}
