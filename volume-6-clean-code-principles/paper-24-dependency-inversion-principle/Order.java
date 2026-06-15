public class Order {
    private String id;
    private double total;
    private String customerEmail;
    private boolean premiumCustomer;

    public Order(String id, double total, String customerEmail, boolean premiumCustomer) {
        this.id = id;
        this.total = total;
        this.customerEmail = customerEmail;
        this.premiumCustomer = premiumCustomer;
    }

    public String getId() { return id; }
    public double getTotal() { return total; }
    public String getCustomerEmail() { return customerEmail; }
    public boolean isPremiumCustomer() { return premiumCustomer; }

    @Override
    public String toString() {
        return "Order{id='" + id + "', total=" + total + ", email='" + customerEmail + "'}";
    }
}
