import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A DataLoader-style batched resolver: instead of resolving each post's
 * author field independently (the pressure scenario) or memoizing exact
 * duplicate keys (the wrong-direction scenario), it first collects every
 * distinct author ID needed across the ENTIRE set of posts in this query,
 * gathering the same set of keys a real DataLoader's batch window would
 * collect -- but doing so synchronously here rather than via an async
 * tick -- and issues exactly one batched fetch for all of them, before
 * resolving each post's field from the batch result.
 */
public class BatchingPostAuthorResolver {
    private final AuthorRepository authorRepository;

    public BatchingPostAuthorResolver(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    public void resolveAuthors(List<Post> posts) {
        Set<String> distinctAuthorIds = posts.stream()
                .map(post -> post.authorId)
                .collect(Collectors.toCollection(HashSet::new));

        Map<String, Author> authorsById = authorRepository.findByIds(distinctAuthorIds);

        for (Post post : posts) {
            Author author = authorsById.get(post.authorId);
            System.out.println("  Post '" + post.title + "' -> author: " + author);
        }
    }
}
