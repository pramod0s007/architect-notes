// Simple domain POJO — represents a confirmed e-commerce order.

import java.util.List;

public class Order {

    private final String       id;
    private final String       customerName;
    private final List<String> items;
    private final double       total;

    public Order(String id, String customerName, List<String> items, double total) {
        this.id           = id;
        this.customerName = customerName;
        this.items        = List.copyOf(items);
        this.total        = total;
    }

    public String       getId()           { return id; }
    public String       getCustomerName() { return customerName; }
    public List<String> getItems()        { return items; }
    public double       getTotal()        { return total; }

    @Override
    public String toString() {
        return "Order{id='" + id + "', customer='" + customerName
                + "', total=" + total + "}";
    }
}
