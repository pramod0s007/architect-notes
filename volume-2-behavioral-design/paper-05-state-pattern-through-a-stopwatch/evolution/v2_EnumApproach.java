// =============================================================================
// v2 — ENUM APPROACH (Month 3)
// =============================================================================
// Domain: Vending Machine
//
// Integer constants → enum. Better readability and type safety.
// But behavior is STILL SCATTERED across methods.
//
// This is a common intermediate step: teams switch from int flags to enum,
// feel like they've made progress, and stop there. The structural problem
// (behavior spread across methods, each method checking all states) remains.
//
// The same state machine logic exists — it's just slightly safer to read.
// Adding a new state still requires opening every method.
// =============================================================================

public class v2_EnumApproach {

    enum MachineState {
        IDLE, COIN_INSERTED, DISPENSING
    }

    static class VendingMachine {
        private MachineState state = MachineState.IDLE;
        private int coinAmount = 0;
        private int stockCount;
        private final String productName;

        public VendingMachine(String productName, int stock) {
            this.productName = productName;
            this.stockCount  = stock;
        }

        // Better: readable switch/if-else with named states
        // Still bad: behavior is scattered — each method owns part of each state's logic

        public void insertCoin(int amount) {
            switch (state) {
                case IDLE:
                    coinAmount = amount;
                    state = MachineState.COIN_INSERTED;
                    System.out.println("Coin inserted: " + amount + " cents.");
                    break;
                case COIN_INSERTED:
                    // [mild smell] Adding coin to existing amount — edge case behavior
                    coinAmount += amount;
                    System.out.println("Added " + amount + " cents. Total: " + coinAmount);
                    break;
                case DISPENSING:
                    System.out.println("Machine is dispensing. Please wait.");
                    break;
            }
        }

        public void selectProduct() {
            switch (state) {
                case IDLE:
                    System.out.println("Please insert a coin first.");
                    break;
                case COIN_INSERTED:
                    if (stockCount <= 0) {
                        System.out.println("Out of stock. Returning " + coinAmount + " cents.");
                        coinAmount = 0;
                        state = MachineState.IDLE;
                    } else {
                        System.out.println("Dispensing: " + productName);
                        state = MachineState.DISPENSING;
                        doDispense();
                    }
                    break;
                case DISPENSING:
                    System.out.println("Already dispensing.");
                    break;
            }
        }

        public void cancel() {
            switch (state) {
                case IDLE:
                    System.out.println("Nothing to cancel.");
                    break;
                case COIN_INSERTED:
                    System.out.println("Cancelled. Returning " + coinAmount + " cents.");
                    coinAmount = 0;
                    state = MachineState.IDLE;
                    break;
                case DISPENSING:
                    System.out.println("Cannot cancel — dispensing in progress.");
                    break;
            }
        }

        private void doDispense() {
            stockCount--;
            System.out.println("Dispensed: " + productName + ". Stock left: " + stockCount);
            coinAmount = 0;
            state = MachineState.IDLE;
        }

        public MachineState getState() { return state; }
    }

    public static void main(String[] args) {
        VendingMachine vm = new VendingMachine("Chips", 3);

        System.out.println("=== Normal purchase ===");
        vm.insertCoin(75);
        vm.selectProduct();

        System.out.println("\n=== State: " + vm.getState() + " ===");

        System.out.println("\n=== Add more coins ===");
        vm.insertCoin(50);
        vm.insertCoin(25); // adds to existing

        System.out.println("\n=== Cancel ===");
        vm.cancel();

        // Benefit over v1: state names are readable (COIN_INSERTED vs 1)
        // Limitation: same behavior-scattering problem as v1
        // Adding OUT_OF_STOCK state → open all 3 methods, add a new case to each
    }
}
