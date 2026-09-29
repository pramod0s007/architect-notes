import java.util.List;

/**
 * A naive GraphQL field resolver: it does exactly what the GraphQL
 * execution model asks of it -- resolve each post's "author" field
 * independently, one post at a time. Nothing here is wrong in isolation;
 * the cost only becomes visible once a query asks for many posts at once.
 */
public class PostAuthorResolver {
    private final AuthorRepository authorRepository;

    public PostAuthorResolver(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    /** Resolves the "author" field for every post in the list, one round trip per post. */
    public void resolveAuthors(List<Post> posts) {
        for (Post post : posts) {
            Author author = authorRepository.findById(post.authorId);
            System.out.println("  Post '" + post.title + "' -> author: " + author);
        }
    }
}
