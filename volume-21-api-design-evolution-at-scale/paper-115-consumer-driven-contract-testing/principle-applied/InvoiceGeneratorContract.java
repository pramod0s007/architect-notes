import java.util.Map;

/**
 * InvoiceGenerator's own contract, authored by the team that owns
 * InvoiceGenerator: it directly runs InvoiceGenerator's real decision logic
 * and asserts the real behavior it depends on, for a shipped order.
 */
public class InvoiceGeneratorContract implements ConsumerContract {
    private final InvoiceGenerator invoiceGenerator = new InvoiceGenerator();

    @Override
    public String consumerName() {
        return "InvoiceGenerator";
    }

    @Override
    public String scenarioKey() {
        return "SHIPPED";
    }

    @Override
    public void verify(Map<String, Object> shippedOrderResponse) throws ContractViolationException {
        String result = invoiceGenerator.decide(shippedOrderResponse);
        if (!result.startsWith("INVOICE GENERATED")) {
            throw new ContractViolationException(
                    "InvoiceGenerator expected a shipped order to produce an invoice, but got: " + result);
        }
    }
}
