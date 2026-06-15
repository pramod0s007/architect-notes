import java.util.List;

/**
 * Minimal repository contract. Defined to allow testing; not to support caching.
 */
public interface UserRepository {

    User findById(String id);

    List<User> findAll();

    void save(User user);
}
