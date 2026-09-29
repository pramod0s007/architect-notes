import java.util.Map;

/**
 * Simulates the provider's own test suite passing on both the old and new
 * response shape, while the real consumer -- never run as part of the
 * provider's build -- silently breaks on the new shape.
 */
public class Main {
    public static void main(String[] args) {
        OrderService service = new OrderService();
        InvoiceGenerator invoiceGenerator = new InvoiceGenerator();

        Map<String, Object> v1 = service.getOrderV1("ORD-1");
        Map<String, Object> v2 = service.getOrderV2("ORD-1");

        System.out.println("=== Provider's own test suite (checks only 'id' and 'total') ===");
        System.out.println("Against v1 response: " + (ProviderOwnTests.run(v1) ? "PASS" : "FAIL"));
        System.out.println("Against v2 response (status renamed to orderStatus): "
                + (ProviderOwnTests.run(v2) ? "PASS" : "FAIL"));
        System.out.println("Provider's build is green both times -- 'status' was never part of what it checks.");

        System.out.println();
        System.out.println("=== The real consumer (InvoiceGenerator), which the provider never runs ===");
        String v1Result = invoiceGenerator.decide(v1);
        System.out.println("Against v1 response: " + v1Result);
        String v2Result = invoiceGenerator.decide(v2);
        System.out.println("Against v2 response: " + v2Result);

        if (v2Result.startsWith("no invoice")) {
            System.out.println();
            System.out.println("CONFIRMED FAILURE: the provider's test suite was green on both versions of the "
                    + "response, but the real consumer silently stopped generating invoices for shipped orders, "
                    + "because the field it actually reads ('status') was renamed to 'orderStatus' -- a change "
                    + "the provider's own tests had no way to catch, since they never modeled what any consumer "
                    + "actually reads.");
        }
    }
}
