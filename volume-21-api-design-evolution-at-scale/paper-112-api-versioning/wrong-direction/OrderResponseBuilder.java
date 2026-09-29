/**
 * The team's fix attempt after the pressure scenario's incident: "let's be
 * additive -- never remove or rename an existing field, just add new ones
 * for new behavior." They add a new "totalWithTax" field for clients that
 * want the new behavior. But both fields are filled by the SAME shared
 * method that reflects "today's" business rules, on the mistaken belief
 * that reusing one canonical calculation is simply good code hygiene.
 * "total" was never actually pinned back to its original, permanent
 * definition -- it just happens to produce the old-looking number today.
 */
public class OrderResponseBuilder {
    public double total(double subtotal) {
        return currentTotal(subtotal); // MISTAKE: still tracks "today's" rules, not the frozen v1 contract
    }

    public double totalWithTax(double subtotal) {
        return currentTotal(subtotal);
    }

    private double currentTotal(double subtotal) {
        return subtotal * (1 + TaxPolicy.currentRate());
    }
}
