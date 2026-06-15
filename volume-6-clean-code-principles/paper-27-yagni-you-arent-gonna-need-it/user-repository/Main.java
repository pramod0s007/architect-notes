/**
 * YAGNI — user-repository
 *
 * UserService runs against InMemoryUserRepository: clean, fast, zero
 * maintenance burden. PrematureCachingRepository shown to contrast: 80 lines
 * of eviction threads and TTL tracking for a system with 50 users.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== YAGNI: simple InMemoryUserRepository ===");
        UserRepository simple = new InMemoryUserRepository();
        UserService service = new UserService(simple);

        User alice = service.createUser("Alice", "alice@example.com");
        User bob   = service.createUser("Bob",   "bob@example.com");
        service.createUser("Carol", "carol@example.com");

        System.out.println("Find Alice: " + service.findUser(alice.getId()));
        System.out.println("All users : " + service.listUsers().size());
        service.findUser("nonexistent-id");

        System.out.println();
        System.out.println("=== YAGNI violation: premature caching (same result, 80 extra lines) ===");
        PrematureCachingRepository caching = new PrematureCachingRepository();
        UserService cachingService = new UserService(caching);

        cachingService.createUser("Dave", "dave@example.com");
        cachingService.findUser("nonexistent");

        System.out.println("Cache stats (never needed): " + caching.cacheStats());
        System.out.println("Both repositories: same output. Simple version: preferred until load proves otherwise.");
    }
}
