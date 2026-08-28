class Order {
    private final String id;
    private final String customerId;
    private final double total;

    Order(String id, String customerId, double total) {
        this.id = id;
        this.customerId = customerId;
        this.total = total;
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
}
