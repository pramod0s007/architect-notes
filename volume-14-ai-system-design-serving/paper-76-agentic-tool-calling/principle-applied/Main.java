public class Main {
    public static void main(String[] args) {
        OrderTool tool = new OrderTool();
        FixedKeyAgent agent = new FixedKeyAgent(tool);

        System.out.println("One customer action: place one order.");
        System.out.println("First attempt's response is lost in transit (server-side effect still completes).");
        agent.placeOrder();

        System.out.println("Orders actually created: " + tool.totalOrdersCreated());
        System.out.println(tool.totalOrdersCreated() == 1
            ? "FIXED: exactly one order was created. The retry reused the SAME idempotency key, so the "
              + "tool's own deduplication correctly recognized it as the same logical request and did "
              + "nothing new the second time."
            : "Unexpected -- investigate.");
    }
}
