/**
 * A common but fragile attempted fix: catch the second action's
 * failure and manually compensate by undoing the first action (here,
 * refunding the debited payment). This "works" as long as the
 * compensating action itself succeeds -- but the compensating action
 * is just another ordinary call to another service, with no
 * transactional guarantee of its own, and can fail too.
 */
public class Main {
    public static void main(String[] args) {
        runScenario("Case 1: compensating refund succeeds", false);
        runScenario("Case 2: compensating refund also fails", true);
    }

    private static void runScenario(String label, boolean refundGatewayDown) {
        PaymentService payment = new PaymentService("acct-1", 100);
        InventoryService inventory = new InventoryService("widget", 2);
        payment.setRefundGatewayDown(refundGatewayDown);

        int balanceBefore = payment.getBalance("acct-1");

        boolean paymentSucceeded = false;
        boolean inventorySucceeded = false;
        boolean compensationSucceeded = false;

        try {
            payment.debit("acct-1", 40);
            paymentSucceeded = true;
            inventory.reserve("widget", 5); // more than the 2 in stock -- fails
            inventorySucceeded = true;
        } catch (IllegalStateException inventoryFailure) {
            try {
                payment.refund("acct-1", 40);
                compensationSucceeded = true;
            } catch (IllegalStateException refundFailure) {
                compensationSucceeded = false;
            }
        }

        int balanceAfter = payment.getBalance("acct-1");
        boolean fullyConsistent = balanceAfter == balanceBefore;

        System.out.println(label + ":");
        System.out.println("  Payment succeeded: " + paymentSucceeded + ", inventory succeeded: " + inventorySucceeded);
        System.out.println("  Compensating refund succeeded: " + compensationSucceeded);
        System.out.println("  Balance before: " + balanceBefore + ", after: " + balanceAfter);
        System.out.println("  Fully consistent (balance restored): " + fullyConsistent);
    }
}
