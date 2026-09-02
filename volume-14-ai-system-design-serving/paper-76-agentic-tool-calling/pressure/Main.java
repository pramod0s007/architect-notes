public class Main {
    public static void main(String[] args) {
        OrderTool tool = new OrderTool();
        NaiveAgent agent = new NaiveAgent(tool);

        System.out.println("One customer action: place one order.");
        System.out.println("First attempt's response is lost in transit (server-side effect still completes).");
        agent.placeOrder();

        System.out.println("Orders actually created: " + tool.totalOrdersCreated());
        System.out.println(tool.totalOrdersCreated() > 1
            ? "CONFIRMED FAILURE: one customer action produced " + tool.totalOrdersCreated() + " orders. "
              + "The retry used a DIFFERENT idempotency key than the original attempt, so the tool's own "
              + "correct deduplication never got a chance to recognize it as the same logical request."
            : "Unexpected -- investigate.");
    }
}
