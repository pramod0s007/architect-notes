package evolution;

import java.util.List;
import java.util.Optional;

/**
 * SMELL 3 — Premature Abstraction (Interface with One Eternal Implementation)
 *
 * Anti-Pattern: UserRepository is an interface that has had exactly one
 * implementation (JpaUserRepository) for 2 years. No test uses a mock.
 * The interface adds indirection without any of the benefits abstraction
 * is supposed to provide.
 *
 * Cost of the unnecessary interface:
 * - IDE navigation: Ctrl+Click on UserRepository jumps to the interface,
 *   not the code. Requires an extra hop to reach the implementation.
 * - Every new method must be declared in two places.
 * - New developers ask "what are the other implementations?" — there are none.
 * - The interface communicates a future that doesn't exist.
 *
 * Fix: Delete the interface. Use JpaUserRepository directly.
 * If a second implementation genuinely appears, extract the interface then.
 * That takes 30 seconds in any IDE.
 */
public class smell3_PrematureAbstraction {

    // Simulated domain object
    static class User {
        final long   id;
        final String email;
        final String name;

        User(long id, String email, String name) {
            this.id    = id;
            this.email = email;
            this.name  = name;
        }

        @Override public String toString() {
            return "User{id=" + id + ", email='" + email + "', name='" + name + "'}";
        }
    }

    // ---------------------------------------------------------------
    // BEFORE: Interface with one implementation, forever
    // ---------------------------------------------------------------

    /**
     * Interface that has never had a second implementation.
     * Tests use @SpringBootTest with a real DB or H2 — not this interface.
     * The mock that would justify this abstraction was never written.
     */
    interface UserRepository {
        Optional<User> findById(long id);
        Optional<User> findByEmail(String email);
        List<User>     findAll();
        User           save(User user);
        void           deleteById(long id);
        long           count();
    }

    /**
     * The only implementation. Two years old. No sibling classes.
     * Every change requires updating UserRepository AND JpaUserRepository.
     */
    static class JpaUserRepository implements UserRepository {
        @Override
        public Optional<User> findById(long id) {
            // simulate: in real code, calls EntityManager
            System.out.println("  JPA: SELECT * FROM users WHERE id=" + id);
            return Optional.of(new User(id, "alice@corp.com", "Alice"));
        }

        @Override
        public Optional<User> findByEmail(String email) {
            System.out.println("  JPA: SELECT * FROM users WHERE email='" + email + "'");
            return Optional.of(new User(1L, email, "Alice"));
        }

        @Override
        public List<User> findAll() {
            System.out.println("  JPA: SELECT * FROM users");
            return List.of(new User(1L, "alice@corp.com", "Alice"),
                           new User(2L, "bob@corp.com",   "Bob"));
        }

        @Override
        public User save(User user) {
            System.out.println("  JPA: INSERT/UPDATE users SET ...");
            return user;
        }

        @Override
        public void deleteById(long id) {
            System.out.println("  JPA: DELETE FROM users WHERE id=" + id);
        }

        @Override
        public long count() {
            System.out.println("  JPA: SELECT COUNT(*) FROM users");
            return 2;
        }
    }

    /** Service: depends on the interface — but gets the same class every time. */
    static class UserService_Before {
        private final UserRepository repo;   // always JpaUserRepository

        UserService_Before(UserRepository repo) {
            this.repo = repo;
        }

        User getUserById(long id) {
            return repo.findById(id).orElseThrow(
                    () -> new RuntimeException("User not found: " + id));
        }

        long countUsers() {
            return repo.count();
        }
    }

    // ---------------------------------------------------------------
    // AFTER: Concrete class — direct, honest, navigable
    // ---------------------------------------------------------------

    /**
     * No interface. JpaUserRepository used directly.
     * When (if) a MockUserRepository or MongoUserRepository ever appears,
     * extract the interface at that point — IDEs do it in one keystroke.
     */
    static class UserService_After {
        private final JpaUserRepository repo;

        UserService_After(JpaUserRepository repo) {
            this.repo = repo;
        }

        User getUserById(long id) {
            return repo.findById(id).orElseThrow(
                    () -> new RuntimeException("User not found: " + id));
        }

        long countUsers() {
            return repo.count();
        }
    }

    // ---------------------------------------------------------------
    // Main
    // ---------------------------------------------------------------
    public static void main(String[] args) {
        System.out.println("=== Smell 3: Premature Abstraction ===\n");

        System.out.println("--- BEFORE: Service depends on UserRepository interface ---");
        JpaUserRepository jpa = new JpaUserRepository();
        UserService_Before before = new UserService_Before(jpa);
        System.out.println("Found: " + before.getUserById(1L));
        System.out.println("Count: " + before.countUsers());

        System.out.println();
        System.out.println("--- AFTER: Service uses JpaUserRepository directly ---");
        UserService_After after = new UserService_After(new JpaUserRepository());
        System.out.println("Found: " + after.getUserById(1L));
        System.out.println("Count: " + after.countUsers());

        System.out.println();
        System.out.println("Both produce the same output.");
        System.out.println("The interface added zero flexibility, doubled the maintenance surface.");
        System.out.println();
        System.out.println("Rule: extract an interface when the second implementation exists,");
        System.out.println("      or when tests genuinely need a different implementation.");
        System.out.println("      Not before. The IDE will help you extract it in 10 seconds.");
    }
}
