import java.util.HashMap;
import java.util.Map;

// Standing in for "what the payment provider actually charged" — visible only
// to whatever computed the amount. Here, that's the gateway alone.
class ChargeLog {
    private static final Map<String, Double> chargedAmountsByOrderId = new HashMap<>();

    static void record(String orderId, double amount) {
        chargedAmountsByOrderId.put(orderId, amount);
    }

    static double amountChargedFor(String orderId) {
        return chargedAmountsByOrderId.getOrDefault(orderId, -1.0);
    }
}
