// A mutable order record. status changes over the order's lifecycle --
// unlike Paper 90's read-only example, this paper is specifically about what
// happens when a resource has MULTIPLE operations (read, update) and each
// one needs its own ownership check, not just the one that got tested.
class Order {
    final String orderId;
    final String ownerId;
    String status;

    Order(String orderId, String ownerId, String status) {
        this.orderId = orderId;
        this.ownerId = ownerId;
        this.status = status;
    }
}
