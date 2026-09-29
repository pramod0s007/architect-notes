import java.util.Map;

/**
 * The provider team's fix after the previous incident: a hand-authored
 * integration test that explicitly checks "status" is present and correct
 * -- learning from what broke last time. But it only ever exercises a
 * shipped order, because that's the scenario the provider team knows
 * about; it was never told that a cancelled order's exact status string
 * is load-bearing for a different consumer entirely.
 */
public class ProviderIntegrationTests {
    public static boolean run(Map<String, Object> shippedOrderResponse) {
        boolean idPresent = shippedOrderResponse.get("id") != null;
        boolean totalPositive = shippedOrderResponse.get("total") != null
                && (double) shippedOrderResponse.get("total") > 0;
        boolean statusCorrect = "SHIPPED".equals(shippedOrderResponse.get("status"));
        return idPresent && totalPositive && statusCorrect;
    }
}
