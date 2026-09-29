/**
 * The permanently frozen /v1/orders handler. Its "total" field is defined,
 * by construction, to never depend on today's business rules -- it is
 * hardcoded to the original published contract (subtotal only, excludes
 * tax) and cannot drift no matter what TaxPolicy does in the future.
 */
public class OrderHandlerV1 {
    public double total(double subtotal) {
        return subtotal; // frozen forever: this is the entire v1 contract, not a snapshot of "today's" logic
    }
}
