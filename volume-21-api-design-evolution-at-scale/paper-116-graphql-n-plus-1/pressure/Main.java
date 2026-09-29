import java.util.List;

/**
 * Simulates a single GraphQL query asking for a feed of posts, each with
 * its author field selected -- exactly the shape of query that triggers
 * the N+1 problem: 1 query for the posts, plus 1 round trip per post to
 * resolve its author.
 */
public class Main {
    public static void main(String[] args) {
        List<Author> authors = List.of(
                new Author("A1", "Alice"),
                new Author("A2", "Bob"),
                new Author("A3", "Carol"),
                new Author("A4", "Dave"),
                new Author("A5", "Eve"),
                new Author("A6", "Frank"));

        List<Post> posts = List.of(
                new Post("P1", "Intro to GraphQL", "A1"),
                new Post("P2", "Scaling Postgres", "A2"),
                new Post("P3", "Rust for Backend Devs", "A3"),
                new Post("P4", "Kubernetes at Scale", "A4"),
                new Post("P5", "Observability 101", "A5"),
                new Post("P6", "Event Sourcing", "A6"),
                new Post("P7", "GraphQL Federation", "A1"),
                new Post("P8", "API Gateways", "A2"));

        AuthorRepository authorRepository = new AuthorRepository(authors);
        PostAuthorResolver resolver = new PostAuthorResolver(authorRepository);

        System.out.println("=== Single GraphQL query: 8 posts, each with its author field selected ===");
        resolver.resolveAuthors(posts);

        System.out.println();
        System.out.println("1 query to fetch the 8 posts, plus " + authorRepository.callCount()
                + " separate round trips to resolve each post's author field.");
        System.out.println("CONFIRMED N+1: a single logical query for a feed of N posts cost 1 + N round trips "
                + "to the datastore, because the GraphQL execution model resolves each post's author field "
                + "independently, with no visibility into the fact that 8 posts only needed at most 8 (and "
                + "here, only 6 distinct) author lookups.");
    }
}
