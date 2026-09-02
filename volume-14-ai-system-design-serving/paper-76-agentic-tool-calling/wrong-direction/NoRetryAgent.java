import java.util.UUID;

// The overcorrection: retries are risky, so never retry at all -- on any
// perceived failure, report the action as failed and stop.
class NoRetryAgent {
    private final OrderTool tool;
    private Boolean lastReportedSuccess;

    NoRetryAgent(OrderTool tool) {
        this.tool = tool;
    }

    void placeOrder() {
        String key = UUID.randomUUID().toString();
        boolean gotResponse = tool.call(key);
        lastReportedSuccess = gotResponse;
    }

    boolean reportedSuccessToCustomer() {
        if (lastReportedSuccess == null) {
            throw new IllegalStateException("placeOrder() must be called before checking its result");
        }
        return lastReportedSuccess;
    }
}
