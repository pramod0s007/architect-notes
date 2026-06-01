// =============================================================================
// v1 — NO PATTERN (Month 1)
// =============================================================================
// Domain: Vending Machine
//
// VendingMachine with 3 states modeled as integer constants.
// Simple, correct, no pattern needed yet.
//
// Comments: "correct as-is, no pressure yet"
// =============================================================================

public class v1_NoPattern {

    static class VendingMachine {
        // State constants — integer flags
        private static final int IDLE           = 0;
        private static final int COIN_INSERTED  = 1;
        private static final int DISPENSING     = 2;

        private int state = IDLE;
        private int coinAmount = 0;
        private final String productName;

        public VendingMachine(String productName) {
            this.productName = productName;
        }

        // Actions
        public void insertCoin(int amount) {
            if (state == IDLE) {
                coinAmount = amount;
                state = COIN_INSERTED;
                System.out.println("Coin inserted: " + amount + " cents. Select a product.");
            } else {
                System.out.println("Already has coin. State=" + state);
            }
        }

        public void selectProduct() {
            if (state == COIN_INSERTED) {
                System.out.println("Product selected: " + productName + ". Dispensing...");
                state = DISPENSING;
                dispense();
            } else {
                System.out.println("Please insert a coin first. State=" + state);
            }
        }

        public void cancel() {
            if (state == COIN_INSERTED) {
                System.out.println("Cancelled. Returning " + coinAmount + " cents.");
                coinAmount = 0;
                state = IDLE;
            } else {
                System.out.println("Nothing to cancel. State=" + state);
            }
        }

        private void dispense() {
            if (state == DISPENSING) {
                System.out.println("Dispensed: " + productName);
                coinAmount = 0;
                state = IDLE;
            }
        }
    }

    public static void main(String[] args) {
        VendingMachine vm = new VendingMachine("Cola");

        System.out.println("=== Normal purchase ===");
        vm.insertCoin(100);
        vm.selectProduct();

        System.out.println("\n=== Cancel after coin ===");
        vm.insertCoin(50);
        vm.cancel();

        System.out.println("\n=== Invalid sequence ===");
        vm.selectProduct(); // no coin
        vm.insertCoin(25);
        vm.insertCoin(25);  // already has coin
    }
}
