import java.util.Map;

/**
 * Simulates the provider's hand-authored integration test staying green
 * across a change that only ever touches cancelled orders -- a scenario
 * the test never exercises -- while a real, untracked consumer
 * (RefundProcessor) silently breaks.
 */
public class Main {
    public static void main(String[] args) {
        OrderService service = new OrderService();
        RefundProcessor refundProcessor = new RefundProcessor();

        Map<String, Object> shipped = service.getShippedOrderV1("ORD-1");
        Map<String, Object> cancelledV1 = service.getCancelledOrderV1("ORD-2");
        Map<String, Object> cancelledV2 = service.getCancelledOrderV2("ORD-2");

        System.out.println("=== Provider's hand-authored integration test (only exercises a SHIPPED order) ===");
        System.out.println("Before the spelling cleanup: " + (ProviderIntegrationTests.run(shipped) ? "PASS" : "FAIL"));
        System.out.println("After the spelling cleanup (which never touched shipped orders at all): "
                + (ProviderIntegrationTests.run(shipped) ? "PASS" : "FAIL"));
        System.out.println("The integration test is green both times -- it never once exercised a cancelled order.");

        System.out.println();
        System.out.println("=== The real consumer (RefundProcessor), untracked by the provider's test ===");
        String beforeResult = refundProcessor.decide(cancelledV1);
        System.out.println("Against the original 'CANCELLED' status: " + beforeResult);
        String afterResult = refundProcessor.decide(cancelledV2);
        System.out.println("Against the 'CANCELED' status after the spelling cleanup: " + afterResult);

        if (afterResult.startsWith("no refund")) {
            System.out.println();
            System.out.println("CONFIRMED FAILURE: the provider's integration test stayed green through the "
                    + "entire change, because it only ever tested a shipped order -- but a real consumer that "
                    + "depends on the exact spelling of the cancelled status silently stopped issuing refunds. "
                    + "A hand-authored test still only encodes what the provider team thought to check; it "
                    + "doesn't know what it doesn't know about a consumer it was never told existed.");
        }
    }
}
