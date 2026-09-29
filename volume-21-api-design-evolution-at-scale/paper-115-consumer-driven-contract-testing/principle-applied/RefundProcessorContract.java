import java.util.Map;

/**
 * RefundProcessor's own contract, authored by the team that owns
 * RefundProcessor: it directly runs RefundProcessor's real decision logic
 * and asserts the real behavior it depends on, for a cancelled order.
 * This is the exact expectation the provider's hand-authored integration
 * test never encoded, because the provider team didn't know it existed.
 */
public class RefundProcessorContract implements ConsumerContract {
    private final RefundProcessor refundProcessor = new RefundProcessor();

    @Override
    public String consumerName() {
        return "RefundProcessor";
    }

    @Override
    public String scenarioKey() {
        return "CANCELLED";
    }

    @Override
    public void verify(Map<String, Object> cancelledOrderResponse) throws ContractViolationException {
        String result = refundProcessor.decide(cancelledOrderResponse);
        if (!result.startsWith("REFUND ISSUED")) {
            throw new ContractViolationException(
                    "RefundProcessor expected a cancelled order to trigger a refund, but got: " + result);
        }
    }
}
