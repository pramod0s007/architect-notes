/** Generated stub matching server v1's schema: tag 1 is exposed as totalPrice(). */
public class OrderTotalStubV1 {
    private final WireMessage message;

    OrderTotalStubV1(WireMessage message) {
        this.message = message;
    }

    double totalPrice() {
        return (double) message.get(1);
    }
}
