import java.util.UUID;

// The obvious first implementation of a retrying tool-call wrapper: on any
// failure or timeout, just try again. "Try again" naturally reads as "make a
// new attempt" -- which, written carelessly, means a fresh idempotency key
// every time, since each attempt looks like a brand new call.
class NaiveAgent {
    private final OrderTool tool;

    NaiveAgent(OrderTool tool) {
        this.tool = tool;
    }

    void placeOrder() {
        String key = UUID.randomUUID().toString();
        boolean gotResponse = tool.call(key);
        if (!gotResponse) {
            // Perceived timeout -- retry with a NEW key, since this "feels like"
            // a fresh attempt.
            String retryKey = UUID.randomUUID().toString();
            tool.call(retryKey);
        }
    }
}
