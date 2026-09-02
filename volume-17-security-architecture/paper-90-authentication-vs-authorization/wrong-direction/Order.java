// One order record. ownerId is the ONLY fact that determines who is allowed
// to see it -- authentication proves who is making the request; this field
// is what authorization is supposed to check the request against.
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
