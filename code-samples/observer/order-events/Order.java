package observer.orderevents;

public class Order {
    private final String id;
    private final String customerId;
    private final String customerEmail;
    private final int itemCount;
    private final double totalValue;

    public Order(String id, String customerId, String customerEmail,
                 int itemCount, double totalValue) {
        this.id = id;
        this.customerId = customerId;
        this.customerEmail = customerEmail;
        this.itemCount = itemCount;
        this.totalValue = totalValue;
    }

    public String getId()            { return id; }
    public String getCustomerId()    { return customerId; }
    public String getCustomerEmail() { return customerEmail; }
    public int getItemCount()        { return itemCount; }
    public double getTotalValue()    { return totalValue; }
}
