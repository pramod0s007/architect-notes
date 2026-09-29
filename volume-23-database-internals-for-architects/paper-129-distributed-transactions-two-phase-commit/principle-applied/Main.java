/**
 * Demonstrates real two-phase commit: both participants prepare
 * (tentatively hold their resource and vote) before either one
 * commits anything permanently. If either participant votes false,
 * both are aborted -- including a participant that already prepared
 * successfully -- so no partial state is ever left behind, regardless
 * of which participant fails.
 */
public class Main {
    public static void main(String[] args) {
        runScenario("Case 1: both participants can prepare", 2);
        runScenario("Case 2: inventory can't prepare (insufficient stock)", 5);
    }

    private static void runScenario(String label, int quantityRequested) {
        PaymentService payment = new PaymentService("acct-1", 100);
        InventoryService inventory = new InventoryService("widget", 2);
        String txnId = "txn-1";

        int balanceBefore = payment.getBalance("acct-1");
        int stockBefore = inventory.getStock("widget");

        boolean paymentPrepared = payment.prepare(txnId, "acct-1", 40);
        boolean inventoryPrepared = inventory.prepare(txnId, "widget", quantityRequested);
        boolean allPrepared = paymentPrepared && inventoryPrepared;

        if (allPrepared) {
            payment.commit(txnId);
            inventory.commit(txnId);
        } else {
            // Abort every participant that voted yes, including ones
            // that already placed a tentative hold -- a "no" vote from
            // any single participant means nothing gets to commit.
            if (paymentPrepared) {
                payment.abort("acct-1", txnId);
            }
            if (inventoryPrepared) {
                inventory.abort("widget", txnId);
            }
        }

        int balanceAfter = payment.getBalance("acct-1");
        int stockAfter = inventory.getStock("widget");
        boolean consistent = allPrepared
                ? (balanceAfter < balanceBefore && stockAfter < stockBefore)
                : (balanceAfter == balanceBefore && stockAfter == stockBefore);

        System.out.println(label + ":");
        System.out.println("  Payment prepared: " + paymentPrepared + ", inventory prepared: " + inventoryPrepared);
        System.out.println("  Transaction committed: " + allPrepared);
        System.out.println("  Balance before: " + balanceBefore + ", after: " + balanceAfter);
        System.out.println("  Stock before: " + stockBefore + ", after: " + stockAfter);
        System.out.println("  Fully consistent (both changed together, or neither changed): " + consistent);
    }
}
