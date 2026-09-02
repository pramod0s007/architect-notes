public class Main {
    public static void main(String[] args) {
        OrderTool tool = new OrderTool();
        NoRetryAgent agent = new NoRetryAgent(tool);

        System.out.println("One customer action: place one order.");
        System.out.println("First attempt's response is lost in transit (server-side effect still completes).");
        agent.placeOrder();

        System.out.println("Orders actually created: " + tool.totalOrdersCreated());
        System.out.println("Reported to customer: " + (agent.reportedSuccessToCustomer() ? "SUCCESS" : "FAILED, please try again"));

        boolean mismatch = tool.totalOrdersCreated() == 1 && !agent.reportedSuccessToCustomer();
        System.out.println(mismatch
            ? "STILL FAILS, DIFFERENTLY: the order WAS actually created -- avoiding a retry did prevent "
              + "a duplicate -- but the agent never found out, gave up on the very first ambiguous "
              + "response, and told the customer the order failed. The customer now believes they need "
              + "to try again, which risks a real, human-initiated duplicate order placed completely "
              + "outside this system's control. Refusing to retry didn't resolve the ambiguity -- it "
              + "just reported a guess, and the guess was wrong."
            : "Unexpected -- investigate.");
    }
}
