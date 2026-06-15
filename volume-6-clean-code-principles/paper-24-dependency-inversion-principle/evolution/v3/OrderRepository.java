// v3: DIP applied. High-level policy owns these abstractions.
// Infrastructure implementations depend on these interfaces — not the other way around.
public interface OrderRepository {
    void save(Order order);
}
