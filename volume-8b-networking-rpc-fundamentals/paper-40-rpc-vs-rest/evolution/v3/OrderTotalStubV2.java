/**
 * Generated stub matching server v2's schema — the .proto field was renamed from
 * "total_price" to "total_amount", but its wire tag stayed 1. The accessor name changed;
 * the wire identity did not.
 */
public class OrderTotalStubV2 {
    private final WireMessage message;

    OrderTotalStubV2(WireMessage message) {
        this.message = message;
    }

    double totalAmount() {
        return (double) message.get(1);
    }
}
