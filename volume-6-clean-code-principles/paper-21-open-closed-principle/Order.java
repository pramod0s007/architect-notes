public class Order {
    private String id;
    private double amount;
    private String paymentMethod;

    public Order(String id, double amount, String paymentMethod) {
        this.id = id;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
    }

    public String getId() { return id; }
    public double getAmount() { return amount; }
    public String getPaymentMethod() { return paymentMethod; }

    @Override
    public String toString() {
        return "Order{id='" + id + "', amount=" + amount + ", method='" + paymentMethod + "'}";
    }
}
