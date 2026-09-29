/**
 * A client written against the original published contract of the v1
 * "total" field: it excludes tax, so the client applies tax itself before
 * showing a price to the customer. The client's own tax rate is a fixed
 * assumption baked in at the time it was written, NOT a live lookup against
 * whatever the server's current business rules happen to be -- exactly
 * like real client code, which doesn't call back into the server's
 * internal config to double-check its own math.
 */
public class OldClient {
    private static final double ASSUMED_TAX_RATE = 0.08;

    public double renderPriceToCustomer(double serverTotal) {
        return serverTotal * (1 + ASSUMED_TAX_RATE);
    }
}
