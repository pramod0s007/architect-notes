import java.util.UUID;

// The fix: generate the idempotency key ONCE, for the logical action, before
// the first attempt -- and reuse that same key for every retry of that same
// action. A retry is the same request being asked again, not a new request.
class FixedKeyAgent {
    private final OrderTool tool;

    FixedKeyAgent(OrderTool tool) {
        this.tool = tool;
    }

    void placeOrder() {
        String key = UUID.randomUUID().toString();
        boolean gotResponse = tool.call(key);
        if (!gotResponse) {
            // Perceived timeout -- retry with the SAME key. If the original call
            // actually succeeded, the tool recognizes the key and does nothing new.
            tool.call(key);
        }
    }
}
