import java.util.List;

// Runs independently of the gateway — has no idea the VIP discount rule exists,
// because that rule lives only inside OverloadedApiGateway.
class BatchReconciliationJob {
    void reconcile(List<Order> orders) {
        for (Order order : orders) {
            double expected = order.getTotal(); // doesn't know about the discount
            double actual = ChargeLog.amountChargedFor(order.getId());
            if (Math.abs(expected - actual) > 0.001) {
                System.out.println("MISMATCH " + order.getId()
                        + ": expected=" + expected + " actual=" + actual);
            } else {
                System.out.println("OK " + order.getId() + ": " + actual);
            }
        }
    }
}
