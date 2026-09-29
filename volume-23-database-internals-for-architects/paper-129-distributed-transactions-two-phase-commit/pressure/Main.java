/**
 * Demonstrates that without any distributed-transaction coordination,
 * two independent actions across two services (debit a payment,
 * reserve inventory) that are supposed to happen together as one
 * logical unit can partially succeed: the first action commits
 * permanently, and the second one then fails, leaving the two
 * services in states that no longer agree with each other.
 */
public class Main {
    public static void main(String[] args) {
        PaymentService payment = new PaymentService("acct-1", 100);
        InventoryService inventory = new InventoryService("widget", 2);

        int balanceBefore = payment.getBalance("acct-1");
        int stockBefore = inventory.getStock("widget");

        boolean paymentSucceeded = false;
        boolean inventorySucceeded = false;

        try {
            payment.debit("acct-1", 40);
            paymentSucceeded = true;
            inventory.reserve("widget", 5); // more than the 2 in stock -- fails
            inventorySucceeded = true;
        } catch (IllegalStateException e) {
            // Inventory failed; the payment already went through and
            // there is no built-in way to undo it from here.
        }

        int balanceAfter = payment.getBalance("acct-1");
        int stockAfter = inventory.getStock("widget");
        boolean inconsistent = paymentSucceeded && !inventorySucceeded;

        System.out.println("Payment succeeded: " + paymentSucceeded);
        System.out.println("Inventory succeeded: " + inventorySucceeded);
        System.out.println("Balance before: " + balanceBefore + ", after: " + balanceAfter);
        System.out.println("Stock before: " + stockBefore + ", after: " + stockAfter);
        System.out.println("Inconsistent state (customer charged, nothing reserved): " + inconsistent);
    }
}
