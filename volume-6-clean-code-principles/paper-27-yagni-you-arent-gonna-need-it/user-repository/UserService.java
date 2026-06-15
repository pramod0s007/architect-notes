import java.util.List;
import java.util.UUID;

/**
 * Business logic: depends on UserRepository interface.
 * Works identically whether backed by the simple or premature-caching implementation.
 */
public class UserService {

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public User createUser(String name, String email) {
        User user = new User(UUID.randomUUID().toString().substring(0, 8), name, email);
        repository.save(user);
        System.out.println("Created: " + user);
        return user;
    }

    public User findUser(String id) {
        User user = repository.findById(id);
        if (user == null) {
            System.out.println("User not found: " + id);
        }
        return user;
    }

    public List<User> listUsers() {
        return repository.findAll();
    }
}
