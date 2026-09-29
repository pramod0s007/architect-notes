import java.util.List;
import java.util.Map;

/**
 * Simulates the provider's build running every real consumer's own
 * contract, each against its own declared scenario, before attempting the
 * same spelling-cleanup change that shipped silently in the
 * wrong-direction scenario -- this time, the change is caught before it
 * can ever reach production.
 */
public class Main {
    public static void main(String[] args) {
        OrderService service = new OrderService();
        ContractTestRunner runner = new ContractTestRunner(
                List.of(new InvoiceGeneratorContract(), new RefundProcessorContract()));

        System.out.println("=== Provider's build: running every registered consumer's own contract, "
                + "against the CURRENT (pre-change) response shapes ===");
        Map<String, Map<String, Object>> beforeChange = Map.of(
                "SHIPPED", service.getShippedOrderV1("ORD-1"),
                "CANCELLED", service.getCancelledOrderV1("ORD-2"));
        boolean allPassedBefore = runner.runAll(beforeChange);
        System.out.println("All contracts pass: " + allPassedBefore);

        System.out.println();
        System.out.println("=== Attempting the same spelling-cleanup change (\"CANCELLED\" -> \"CANCELED\"), "
                + "which never touches the SHIPPED scenario at all ===");
        Map<String, Map<String, Object>> afterChange = Map.of(
                "SHIPPED", service.getShippedOrderV1("ORD-1"),
                "CANCELLED", service.getCancelledOrderV2("ORD-2"));
        boolean allPassedAfter = runner.runAll(afterChange);

        System.out.println();
        if (!allPassedAfter) {
            System.out.println("DEPLOY BLOCKED: RefundProcessor's own contract failed against the candidate "
                    + "change, because RefundProcessor -- not the provider's guess about RefundProcessor -- "
                    + "specified exactly what it needs, checked against the exact scenario it actually "
                    + "depends on. The change never reaches production; the provider's build catches it in "
                    + "the same place a hand-authored, provider-guessed integration test missed it entirely.");
        }
    }
}
