class Order {
    private final String id;
    private final double subtotal;

    Order(String id, double subtotal) {
        this.id = id;
        this.subtotal = subtotal;
    }

    String getId() {
        return id;
    }

    double getSubtotal() {
        return subtotal;
    }

    double getTotal() {
        return subtotal;
    }
}
