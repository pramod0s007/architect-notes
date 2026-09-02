class Order {
    final String orderId;
    final String ownerId;
    final String contents;

    Order(String orderId, String ownerId, String contents) {
        this.orderId = orderId;
        this.ownerId = ownerId;
        this.contents = contents;
    }
}
