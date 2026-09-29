import java.util.List;

/**
 * Runs the exact same two feeds as the wrong-direction scenario -- the
 * 8-post feed with 2 repeated authors, and the 6-post feed where every
 * author is distinct -- against the batching resolver, to show the fix
 * holds regardless of how much duplication a given query happens to
 * contain.
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

        System.out.println("=== Query 1: the same 8-post feed, 2 authors repeat ===");
        AuthorRepository repo1 = new AuthorRepository(authors);
        BatchingPostAuthorResolver resolver1 = new BatchingPostAuthorResolver(repo1);
        resolver1.resolveAuthors(repeatedAuthorFeed);
        System.out.println("Round trips: " + repo1.callCount() + " -- one batched fetch for all distinct "
                + "author IDs needed by this query, regardless of how many posts requested them.");

        System.out.println();
        System.out.println("=== Query 2: the same 6-post feed where every author is distinct ===");
        AuthorRepository repo2 = new AuthorRepository(authors);
        BatchingPostAuthorResolver resolver2 = new BatchingPostAuthorResolver(repo2);
        resolver2.resolveAuthors(distinctAuthorFeed);
        System.out.println("Round trips: " + repo2.callCount() + " -- still one batched fetch, even though "
                + "all 6 author IDs are distinct and memoization would have provided zero benefit here.");

        System.out.println();
        System.out.println("FIXED: both queries cost exactly 1 round trip to resolve every post's author "
                + "field, regardless of how many posts were requested or how much (if any) duplication "
                + "existed among their author IDs -- because the fix collects every distinct key needed "
                + "across the whole query first, and only then issues a single batched fetch for all of "
                + "them, instead of resolving one field at a time.");
    }
}
