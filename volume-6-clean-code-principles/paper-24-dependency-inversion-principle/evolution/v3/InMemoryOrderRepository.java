import java.util.ArrayList;
import java.util.List;

// Test double: no database, no network. Fast, deterministic, zero setup.
public class InMemoryOrderRepository implements OrderRepository {

    private final List<Order> saved = new ArrayList<>();

    @Override
    public void save(Order order) {
        saved.add(order);
        System.out.println("In-memory: stored order " + order.getId());
    }

    public List<Order> getSaved() {
        return saved;
    }
}
