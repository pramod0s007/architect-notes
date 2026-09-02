import java.util.HashMap;
import java.util.Map;

// The external tool an agent calls to place an order. It already implements
// idempotency CORRECTLY on its own side -- a duplicate call with the same key
// never creates a second order. The bug in this section is entirely on the
// CALLER's side: whether the same key is actually reused across a retry.
class OrderTool {
    private final Map<String, String> ordersByKey = new HashMap<>();
    private int callCount = 0;

    // Returns true if the caller receives a response; false if the response is
    // lost in transit -- simulating a real, common failure mode where the
    // server-side effect completes but the network drops the acknowledgment.
    // The very first call ever made to this tool has its response lost.
    boolean call(String idempotencyKey) {
        callCount++;
        ordersByKey.putIfAbsent(idempotencyKey, "order-for-" + idempotencyKey);
        boolean responseLost = (callCount == 1);
        return !responseLost;
    }

    int totalOrdersCreated() {
        return ordersByKey.size();
    }
}
