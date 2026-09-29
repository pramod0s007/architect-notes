/**
 * The SAME published endpoint (still called "v1", still the same URL, same
 * field name "total") after an in-place semantic change: someone decided
 * "total" should now include tax, and shipped it without any version bump,
 * reasoning that no field was renamed or removed so nothing could break.
 */
public class OrderServiceV1Modified {
    public double getTotal(double subtotal) {
        return subtotal * (1 + TaxPolicy.currentRate()); // now silently includes tax
    }
}
