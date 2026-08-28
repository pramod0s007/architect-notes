import java.util.List;

class Order {
    private final String id;
    private final String customerId;
    private final double total;
    private final List<String> items;

    Order(String id, String customerId, double total, List<String> items) {
        this.id = id;
        this.customerId = customerId;
        this.total = total;
        this.items = items;
    }

    String getId() {
        return id;
    }

    String getCustomerId() {
        return customerId;
    }

    double getTotal() {
        return total;
    }

    List<String> getItems() {
        return items;
    }

    boolean isValid() {
        return total > 0 && !items.isEmpty();
    }
}
