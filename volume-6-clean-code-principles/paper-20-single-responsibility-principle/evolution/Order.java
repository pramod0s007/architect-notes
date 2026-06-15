import java.util.List;

// Shared stub for evolution examples — shows the domain object being processed
public class Order {
    private final String id;
    private final Customer customer;
    private final List<String> items;
    private final double total;

    public Order(String id, Customer customer, List<String> items, double total) {
        this.id = id; this.customer = customer; this.items = items; this.total = total;
    }

    public String getId()         { return id; }
    public Customer getCustomer() { return customer; }
    public List<String> getItems(){ return items; }
    public double getTotal()      { return total; }
}
