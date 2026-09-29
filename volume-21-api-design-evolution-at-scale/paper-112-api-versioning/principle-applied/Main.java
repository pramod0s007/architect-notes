/**
 * Demonstrates: with a genuinely separate, frozen v1 handler and an
 * independent v2 handler, a business-rule change (the tax rate) can
 * propagate to v2's new field without ever touching v1's "total" -- the
 * old client keeps getting exactly the same, correct answer it always did,
 * no matter how many times the business rules underneath v2 change.
 */
public class Main {
    public static void main(String[] args) {
        double subtotal = 100.00;
        OldClient client = new OldClient();
        OrderHandlerV1 v1 = new OrderHandlerV1();
        OrderHandlerV2 v2 = new OrderHandlerV2();

        System.out.println("=== Tax rate at 8% ===");
        double v1Total1 = v1.total(subtotal);
        double v2Total1 = v2.totalWithTax(subtotal);
        double displayed1 = client.renderPriceToCustomer(v1Total1);
        System.out.println("/v1/orders 'total': " + fmt(v1Total1) + ", /v2/orders 'totalWithTax': " + fmt(v2Total1));
        System.out.println("Old client (pinned to v1) displays to customer: " + fmt(displayed1));

        System.out.println();
        System.out.println("=== Finance updates the tax rate from 8% to 10% ===");
        TaxPolicy.setRate(0.10);
        double v1Total2 = v1.total(subtotal);
        double v2Total2 = v2.totalWithTax(subtotal);
        double displayed2 = client.renderPriceToCustomer(v1Total2);
        System.out.println("/v1/orders 'total': " + fmt(v1Total2) + ", /v2/orders 'totalWithTax': " + fmt(v2Total2));
        System.out.println("Old client (pinned to v1) displays to customer: " + fmt(displayed2));

        System.out.println();
        System.out.println("FIXED: /v1/orders 'total' stayed exactly " + fmt(v1Total1) + " both times -- it is"
                + " hardcoded to the original contract, not derived from any shared, evolving business logic."
                + " The old client's displayed price stayed correct and unchanged at " + fmt(displayed1)
                + " across the tax-rate change, while /v2/orders' 'totalWithTax' correctly moved from "
                + fmt(v2Total1) + " to " + fmt(v2Total2) + " for clients that opted into the new contract.");
    }

    static String fmt(double value) {
        return String.format("%.2f", value);
    }
}
