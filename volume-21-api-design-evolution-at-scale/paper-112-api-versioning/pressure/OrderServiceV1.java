/**
 * The original, published order-total endpoint. Its contract, as every
 * existing client was written against, is: "total" is the subtotal only,
 * BEFORE tax -- the client is expected to add tax itself.
 */
public class OrderServiceV1 {
    public double getTotal(double subtotal) {
        return subtotal; // contract: excludes tax, always has
    }
}
