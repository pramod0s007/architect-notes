import java.util.List;

/**
 * Simulates two queries against the "fixed" resolver: the same 8-post
 * feed from the pressure scenario (which happens to reuse 2 authors
 * across posts), and a second, equally realistic 6-post feed where every
 * post happens to be written by a different author -- the common shape
 * of a public feed with many distinct contributors.
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

        List<Post> repeatedAuthorFeed = List.of(
                new Post("P1", "Intro to GraphQL", "A1"),
                new Post("P2", "Scaling Postgres", "A2"),
                new Post("P3", "Rust for Backend Devs", "A3"),
                new Post("P4", "Kubernetes at Scale", "A4"),
                new Post("P5", "Observability 101", "A5"),
                new Post("P6", "Event Sourcing", "A6"),
                new Post("P7", "GraphQL Federation", "A1"),
                new Post("P8", "API Gateways", "A2"));

        List<Post> distinctAuthorFeed = List.of(
                new Post("P9", "Circuit Breakers", "A1"),
                new Post("P10", "Blue-Green Deploys", "A2"),
                new Post("P11", "Consensus Algorithms", "A3"),
                new Post("P12", "Distributed Tracing", "A4"),
                new Post("P13", "Schema Evolution", "A5"),
                new Post("P14", "Backpressure", "A6"));

        System.out.println("=== Query 1: the same 8-post feed as the pressure scenario (2 authors repeat) ===");
        AuthorRepository repo1 = new AuthorRepository(authors);
        MemoizingPostAuthorResolver resolver1 = new MemoizingPostAuthorResolver(repo1);
        resolver1.resolveAuthors(repeatedAuthorFeed);
        System.out.println("Round trips: " + repo1.callCount() + " (down from 8 in the pressure scenario -- "
                + "looks like the fix worked)");

        System.out.println();
        System.out.println("=== Query 2: a 6-post feed where every post happens to be written by a different "
                + "author -- a completely ordinary public-feed shape ===");
        AuthorRepository repo2 = new AuthorRepository(authors);
        MemoizingPostAuthorResolver resolver2 = new MemoizingPostAuthorResolver(repo2);
        resolver2.resolveAuthors(distinctAuthorFeed);
        System.out.println("Round trips: " + repo2.callCount() + " out of 6 posts -- identical to the naive, "
                + "unmemoized resolver from the pressure scenario.");

        System.out.println();
        System.out.println("CONFIRMED WRONG DIRECTION: memoization only collapses round trips for author IDs "
                + "that repeat within the same request. It provided a real, measurable improvement on Query 1 "
                + "purely because that particular feed happened to reuse 2 authors -- and zero improvement on "
                + "Query 2, an equally realistic feed where no author repeats. The fix targeted the symptom "
                + "(duplicate calls), not the structural problem (one round trip per resolved field), so its "
                + "benefit depends entirely on how much duplication a given query happens to contain.");
    }
}
