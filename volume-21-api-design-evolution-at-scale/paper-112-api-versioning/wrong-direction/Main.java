/**
 * Demonstrates: "just add new fields, never touch old ones" sounds like a
 * safe additive-only policy, but if the OLD field's value is still computed
 * from shared, evolving business logic instead of being frozen to its
 * original definition, it still silently changes underneath old clients --
 * merely adding "totalWithTax" alongside it changed nothing for OldClient.
 */
public class Main {
    public static void main(String[] args) {
        double subtotal = 100.00;
        OldClient client = new OldClient();
        OrderResponseBuilder builder = new OrderResponseBuilder();

        System.out.println("=== Right after the 'additive' fix ships (tax rate still 8%) ===");
        double total1 = builder.total(subtotal);
        double totalWithTax1 = builder.totalWithTax(subtotal);
        double displayed1 = client.renderPriceToCustomer(total1);
        System.out.println("'total' field: " + fmt(total1) + " (still wrong -- same bug as before, unfixed), new 'totalWithTax' field: " + fmt(totalWithTax1));
        System.out.println("Client (reads only 'total') displays to customer: " + fmt(displayed1) + " (already overcharging, exactly like before the 'fix')");

        System.out.println();
        System.out.println("=== Finance updates the tax rate from 8% to 10% (an unrelated, legitimate change) ===");
        TaxPolicy.setRate(0.10);
        double total2 = builder.total(subtotal);
        double totalWithTax2 = builder.totalWithTax(subtotal);
        double displayed2 = client.renderPriceToCustomer(total2);
        System.out.println("'total' field: " + fmt(total2) + ", new 'totalWithTax' field: " + fmt(totalWithTax2));
        System.out.println("Client (reads only 'total') displays to customer: " + fmt(displayed2));

        System.out.println();
        System.out.println("CONFIRMED FAILURE: 'total' was supposed to be the frozen, permanent v1 contract"
                + " (subtotal only, " + fmt(subtotal) + "), but it never was -- it shipped already wrong at "
                + fmt(total1) + ", the exact same bug as the pressure scenario, and then drifted again to "
                + fmt(total2) + " when an unrelated tax-rate change landed. Adding 'totalWithTax' alongside it"
                + " did nothing to protect OldClient, because 'total' itself was never actually pinned to its"
                + " original meaning -- it was only ever computed from shared, evolving business logic.");
    }

    static String fmt(double value) {
        return String.format("%.2f", value);
    }
}
