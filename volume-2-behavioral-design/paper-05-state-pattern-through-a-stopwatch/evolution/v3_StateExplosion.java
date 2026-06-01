// =============================================================================
// v3 — STATE EXPLOSION (Month 6)
// =============================================================================
// Domain: Vending Machine
//
// Three new states added over 3 months:
//   - OUT_OF_STOCK (Month 4): machine has no product to dispense
//   - MAINTENANCE (Month 5): technician is servicing the machine
//   - CHANGE_DISPENSE (Month 6): machine is counting and dispensing change
//
// Total: 6 states × 4 actions = 24 case blocks to maintain.
//
// PAIN POINTS (state pressure signals):
//   [!] Every new state forces ALL existing methods to be reopened
//       (insertCoin, selectProduct, cancel, refund must each add a new case)
//   [!] The logic for WHAT each state does is spread across 4 methods —
//       to understand "what can OUT_OF_STOCK do?", you must read all 4 methods
//   [!] Transition logic is mixed with action logic — selectProduct() both
//       decides transitions AND executes behavior
//   [!] The "what action is legal from MAINTENANCE?" question cannot be
//       answered by reading a single class or method
//   [!] Testing: to verify OUT_OF_STOCK.cancel() is a no-op, you must
//       construct a full VendingMachine and walk it to that state first
//
// This is the file where refactoring becomes unavoidable.
// =============================================================================

public class v3_StateExplosion {

    enum MachineState {
        IDLE, COIN_INSERTED, DISPENSING,
        OUT_OF_STOCK,    // Month 4
        MAINTENANCE,     // Month 5
        CHANGE_DISPENSE  // Month 6
    }

    static class VendingMachine {
        private MachineState state = MachineState.IDLE;
        private int coinAmount = 0;
        private int stockCount;
        private final String productName;
        private final int productPrice;

        public VendingMachine(String productName, int price, int stock) {
            this.productName  = productName;
            this.productPrice = price;
            this.stockCount   = stock;
            if (stock == 0) this.state = MachineState.OUT_OF_STOCK;
        }

        // [!] insertCoin: 6 cases. Each new state = open this method.
        public void insertCoin(int amount) {
            switch (state) {
                case IDLE:
                    coinAmount = amount;
                    state = MachineState.COIN_INSERTED;
                    System.out.println("Coin inserted: " + amount + " cents.");
                    break;
                case COIN_INSERTED:
                    coinAmount += amount;
                    System.out.println("Added " + amount + " cents. Total: " + coinAmount);
                    break;
                case DISPENSING:
                    System.out.println("Machine dispensing. Please wait.");
                    break;
                case OUT_OF_STOCK:  // added month 4
                    System.out.println("Out of stock. Coin rejected: " + amount + " cents.");
                    break;
                case MAINTENANCE:   // added month 5
                    System.out.println("Under maintenance. Coin rejected.");
                    break;
                case CHANGE_DISPENSE: // added month 6
                    System.out.println("Dispensing change. Please wait.");
                    break;
            }
        }

        // [!] selectProduct: 6 cases + nested stock/price logic inside COIN_INSERTED
        public void selectProduct() {
            switch (state) {
                case IDLE:
                    System.out.println("Please insert a coin first.");
                    break;
                case COIN_INSERTED:
                    if (coinAmount < productPrice) {
                        System.out.println("Insufficient funds. Need " + (productPrice - coinAmount) + " more cents.");
                    } else if (stockCount <= 0) {
                        System.out.println("Just ran out! Returning " + coinAmount + " cents.");
                        coinAmount = 0;
                        state = MachineState.OUT_OF_STOCK;
                    } else {
                        System.out.println("Dispensing: " + productName);
                        state = MachineState.DISPENSING;
                        doDispense();
                    }
                    break;
                case DISPENSING:
                    System.out.println("Already dispensing.");
                    break;
                case OUT_OF_STOCK:  // added month 4
                    System.out.println("Out of stock. Cannot select product.");
                    break;
                case MAINTENANCE:   // added month 5
                    System.out.println("Under maintenance. Cannot select product.");
                    break;
                case CHANGE_DISPENSE: // added month 6
                    System.out.println("Dispensing change. Please wait for completion.");
                    break;
            }
        }

        // [!] cancel: 6 cases
        public void cancel() {
            switch (state) {
                case IDLE:
                    System.out.println("Nothing to cancel.");
                    break;
                case COIN_INSERTED:
                    System.out.println("Returning " + coinAmount + " cents.");
                    if (coinAmount > productPrice) {
                        state = MachineState.CHANGE_DISPENSE;
                        dispenseChange();
                    } else {
                        returnExactCoins();
                        state = MachineState.IDLE;
                    }
                    break;
                case DISPENSING:
                    System.out.println("Cannot cancel — product is being dispensed.");
                    break;
                case OUT_OF_STOCK:  // added month 4
                    System.out.println("Machine is out of stock. Nothing to cancel.");
                    break;
                case MAINTENANCE:   // added month 5
                    System.out.println("Under maintenance. Service technician manages this.");
                    break;
                case CHANGE_DISPENSE: // added month 6
                    System.out.println("Already dispensing change. Cannot cancel again.");
                    break;
            }
        }

        // [!] enterMaintenance: 6 cases
        public void enterMaintenance() {
            switch (state) {
                case IDLE:
                case OUT_OF_STOCK:
                    System.out.println("Machine entering maintenance mode.");
                    state = MachineState.MAINTENANCE;
                    break;
                case COIN_INSERTED:
                    System.out.println("Returning coin before maintenance: " + coinAmount + " cents.");
                    coinAmount = 0;
                    state = MachineState.MAINTENANCE;
                    break;
                case DISPENSING:
                case CHANGE_DISPENSE:
                    System.out.println("Cannot enter maintenance — operation in progress.");
                    break;
                case MAINTENANCE:
                    System.out.println("Already in maintenance.");
                    break;
            }
        }

        private void doDispense() {
            stockCount--;
            int change = coinAmount - productPrice;
            System.out.println("Dispensed: " + productName);
            coinAmount = 0;
            if (stockCount == 0) {
                state = MachineState.OUT_OF_STOCK;
                System.out.println("Stock exhausted.");
            } else if (change > 0) {
                state = MachineState.CHANGE_DISPENSE;
                coinAmount = change;
                dispenseChange();
            } else {
                state = MachineState.IDLE;
            }
        }

        private void dispenseChange() {
            System.out.println("Dispensing change: " + coinAmount + " cents.");
            coinAmount = 0;
            state = stockCount > 0 ? MachineState.IDLE : MachineState.OUT_OF_STOCK;
        }

        private void returnExactCoins() {
            System.out.println("Returning exact coins: " + coinAmount + " cents.");
            coinAmount = 0;
        }

        public MachineState getState() { return state; }
    }

    public static void main(String[] args) {
        VendingMachine vm = new VendingMachine("Soda", 75, 2);

        System.out.println("=== Normal purchase with change ===");
        vm.insertCoin(100);
        vm.selectProduct();
        System.out.println("State: " + vm.getState());

        System.out.println("\n=== Last item, then out of stock ===");
        vm.insertCoin(75);
        vm.selectProduct();
        System.out.println("State: " + vm.getState());

        System.out.println("\n=== Try to buy when out of stock ===");
        vm.insertCoin(75);
        vm.selectProduct();

        System.out.println("\n=== Maintenance mode ===");
        vm.enterMaintenance();
        vm.insertCoin(50);  // rejected

        // [!] To add a 7th state (e.g. CARD_READER_ERROR), you must:
        //     1. Add to enum
        //     2. Open insertCoin()  — add case
        //     3. Open selectProduct() — add case
        //     4. Open cancel() — add case
        //     5. Open enterMaintenance() — add case
        //     6. Any other action methods — add case
        // With State Pattern: add one class, update only states that transition into it.
    }
}
