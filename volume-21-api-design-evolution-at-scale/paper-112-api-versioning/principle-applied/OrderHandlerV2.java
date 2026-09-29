/**
 * A wholly separate /v2/orders handler for new clients, free to evolve
 * with current business rules. It exposes a differently-named field,
 * "totalWithTax", so there is never any ambiguity about which contract a
 * given field belongs to.
 */
public class OrderHandlerV2 {
    public double totalWithTax(double subtotal) {
        return subtotal * (1 + TaxPolicy.currentRate());
    }
}
