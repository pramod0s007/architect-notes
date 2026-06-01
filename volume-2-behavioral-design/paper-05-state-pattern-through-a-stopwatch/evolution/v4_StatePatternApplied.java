// =============================================================================
// v4 — STATE PATTERN APPLIED
// =============================================================================
// Domain: Vending Machine
//
// WHAT CHANGED from v3:
//   - VendingMachineState interface: every action the machine supports
//   - 6 concrete state classes — each encodes ALL behavior legal from that state
//   - VendingMachineContext delegates all actions to its current state object
//   - Zero switch/if-else in the context class
//
// WHY State Pattern:
//   - "What can this machine do from state X?" is now answerable by reading
//     one class (e.g. OutOfStockState) — not spread across 4+ methods
//   - Adding a 7th state = add one new class, update only the states that
//     transition INTO it. All other state classes are untouched.
//   - Transition logic lives IN the state (e.g. IdleState.selectProduct
//     immediately says "insert coin first") — the source of truth is the state
//   - Illegal transitions throw immediately with a clear message from the state
//     object — no silent no-ops buried in switch cases
//
// WHY NOT Strategy Pattern:
//   - Strategy encapsulates ONE algorithm, swapped externally
//   - State Pattern encapsulates ALL behavior for a state; the state
//     transitions itself — context doesn't decide the next state
//   - In State Pattern, the state objects know about the context (to call
//     setState). In Strategy, strategies are stateless computations.
// =============================================================================

// ---------------------------------------------------------------------------
// Context — the VendingMachine that delegates to state objects
// ---------------------------------------------------------------------------
class VendingMachineContext {
    private VendingMachineState state;
    private int coinAmount = 0;
    private int stockCount;
    private final String productName;
    private final int productPrice;

    public VendingMachineContext(String productName, int price, int stock) {
        this.productName  = productName;
        this.productPrice = price;
        this.stockCount   = stock;
        // Initial state depends on stock
        this.state = (stock > 0) ? new IdleState() : new OutOfStockState();
    }

    // All actions delegated — zero switch/if-else
    public void insertCoin(int amount)   { state.insertCoin(this, amount); }
    public void selectProduct()          { state.selectProduct(this); }
    public void cancel()                 { state.cancel(this); }
    public void enterMaintenance()       { state.enterMaintenance(this); }
    public void exitMaintenance()        { state.exitMaintenance(this); }

    // State accessors — called by state objects to inspect/modify context
    public void setState(VendingMachineState s)  { this.state = s; }
    public int getCoinAmount()                   { return coinAmount; }
    public void setCoinAmount(int amount)        { this.coinAmount = amount; }
    public int getStockCount()                   { return stockCount; }
    public void decrementStock()                 { this.stockCount--; }
    public int getProductPrice()                 { return productPrice; }
    public String getProductName()               { return productName; }
    public String getStateName()                 { return state.name(); }
}

// ---------------------------------------------------------------------------
// State interface
// ---------------------------------------------------------------------------
interface VendingMachineState {
    void insertCoin(VendingMachineContext ctx, int amount);
    void selectProduct(VendingMachineContext ctx);
    void cancel(VendingMachineContext ctx);
    void enterMaintenance(VendingMachineContext ctx);
    void exitMaintenance(VendingMachineContext ctx);
    String name();
}

// ---------------------------------------------------------------------------
// Base class — default: "illegal from this state"
// ---------------------------------------------------------------------------
abstract class AbstractVendingState implements VendingMachineState {
    @Override public void insertCoin(VendingMachineContext ctx, int amount) {
        System.out.println("[" + name() + "] Cannot insert coin. Returned: " + amount + " cents.");
    }
    @Override public void selectProduct(VendingMachineContext ctx) {
        System.out.println("[" + name() + "] Cannot select product.");
    }
    @Override public void cancel(VendingMachineContext ctx) {
        System.out.println("[" + name() + "] Nothing to cancel.");
    }
    @Override public void enterMaintenance(VendingMachineContext ctx) {
        System.out.println("[" + name() + "] Cannot enter maintenance now.");
    }
    @Override public void exitMaintenance(VendingMachineContext ctx) {
        System.out.println("[" + name() + "] Not in maintenance.");
    }
}

// ---------------------------------------------------------------------------
// State 1: IDLE — waiting for a coin
// ---------------------------------------------------------------------------
class IdleState extends AbstractVendingState {
    @Override public String name() { return "IDLE"; }

    @Override
    public void insertCoin(VendingMachineContext ctx, int amount) {
        ctx.setCoinAmount(amount);
        ctx.setState(new CoinInsertedState());
        System.out.println("[IDLE → COIN_INSERTED] " + amount + " cents received.");
    }

    @Override
    public void enterMaintenance(VendingMachineContext ctx) {
        ctx.setState(new MaintenanceState());
        System.out.println("[IDLE → MAINTENANCE] Machine entering maintenance.");
    }
}

// ---------------------------------------------------------------------------
// State 2: COIN_INSERTED — coin received, waiting for product selection
// ---------------------------------------------------------------------------
class CoinInsertedState extends AbstractVendingState {
    @Override public String name() { return "COIN_INSERTED"; }

    @Override
    public void insertCoin(VendingMachineContext ctx, int amount) {
        ctx.setCoinAmount(ctx.getCoinAmount() + amount);
        System.out.println("[COIN_INSERTED] Added " + amount + " cents. Total: " + ctx.getCoinAmount());
    }

    @Override
    public void selectProduct(VendingMachineContext ctx) {
        if (ctx.getCoinAmount() < ctx.getProductPrice()) {
            int need = ctx.getProductPrice() - ctx.getCoinAmount();
            System.out.println("[COIN_INSERTED] Insufficient. Need " + need + " more cents.");
            return;
        }
        System.out.println("[COIN_INSERTED → DISPENSING] Dispensing: " + ctx.getProductName());
        ctx.setState(new DispensingState());
        // Dispense happens immediately — transition then execute
        new DispensingState().doDispense(ctx);
    }

    @Override
    public void cancel(VendingMachineContext ctx) {
        int amount = ctx.getCoinAmount();
        ctx.setCoinAmount(0);
        if (amount > 0) {
            System.out.println("[COIN_INSERTED → CHANGE_DISPENSE] Returning " + amount + " cents.");
            ctx.setState(new ChangeDispenseState(amount));
            new ChangeDispenseState(amount).dispenseChange(ctx);
        } else {
            ctx.setState(new IdleState());
        }
    }

    @Override
    public void enterMaintenance(VendingMachineContext ctx) {
        System.out.println("[COIN_INSERTED → MAINTENANCE] Returning coin: " + ctx.getCoinAmount() + " cents.");
        ctx.setCoinAmount(0);
        ctx.setState(new MaintenanceState());
    }
}

// ---------------------------------------------------------------------------
// State 3: DISPENSING — product is being dispensed
// ---------------------------------------------------------------------------
class DispensingState extends AbstractVendingState {
    @Override public String name() { return "DISPENSING"; }

    // Called by CoinInsertedState after transitioning here
    public void doDispense(VendingMachineContext ctx) {
        ctx.decrementStock();
        int change = ctx.getCoinAmount() - ctx.getProductPrice();
        System.out.println("[DISPENSING] Dispensed: " + ctx.getProductName() +
                           " | Stock left: " + ctx.getStockCount());
        ctx.setCoinAmount(0);

        if (ctx.getStockCount() == 0) {
            System.out.println("[DISPENSING → OUT_OF_STOCK]");
            ctx.setState(new OutOfStockState());
        } else if (change > 0) {
            System.out.println("[DISPENSING → CHANGE_DISPENSE] Change due: " + change + " cents.");
            ctx.setState(new ChangeDispenseState(change));
            new ChangeDispenseState(change).dispenseChange(ctx);
        } else {
            System.out.println("[DISPENSING → IDLE]");
            ctx.setState(new IdleState());
        }
    }
}

// ---------------------------------------------------------------------------
// State 4: OUT_OF_STOCK — no product available
// ---------------------------------------------------------------------------
class OutOfStockState extends AbstractVendingState {
    @Override public String name() { return "OUT_OF_STOCK"; }

    @Override
    public void insertCoin(VendingMachineContext ctx, int amount) {
        System.out.println("[OUT_OF_STOCK] Out of stock. Coin rejected: " + amount + " cents.");
    }

    @Override
    public void enterMaintenance(VendingMachineContext ctx) {
        ctx.setState(new MaintenanceState());
        System.out.println("[OUT_OF_STOCK → MAINTENANCE] Technician servicing machine.");
    }
}

// ---------------------------------------------------------------------------
// State 5: MAINTENANCE — technician is servicing
// ---------------------------------------------------------------------------
class MaintenanceState extends AbstractVendingState {
    @Override public String name() { return "MAINTENANCE"; }

    @Override
    public void exitMaintenance(VendingMachineContext ctx) {
        if (ctx.getStockCount() > 0) {
            ctx.setState(new IdleState());
            System.out.println("[MAINTENANCE → IDLE] Service complete. Machine ready.");
        } else {
            ctx.setState(new OutOfStockState());
            System.out.println("[MAINTENANCE → OUT_OF_STOCK] Service complete. Still out of stock.");
        }
    }
}

// ---------------------------------------------------------------------------
// State 6: CHANGE_DISPENSE — machine counting and returning change
// ---------------------------------------------------------------------------
class ChangeDispenseState extends AbstractVendingState {
    private final int changeAmount;
    public ChangeDispenseState(int changeAmount) { this.changeAmount = changeAmount; }
    @Override public String name() { return "CHANGE_DISPENSE"; }

    public void dispenseChange(VendingMachineContext ctx) {
        System.out.println("[CHANGE_DISPENSE] Dispensing change: " + changeAmount + " cents.");
        if (ctx.getStockCount() > 0) {
            ctx.setState(new IdleState());
        } else {
            ctx.setState(new OutOfStockState());
        }
    }
}

// Expose getState() for demo via a wrapper
class VendingMachineContextExtended extends VendingMachineContext {
    public VendingMachineContextExtended(String p, int price, int stock) {
        super(p, price, stock);
    }
}

// ---------------------------------------------------------------------------
// Demo
// ---------------------------------------------------------------------------
public class v4_StatePatternApplied {
    public static void main(String[] args) {
        VendingMachineContext vm = new VendingMachineContext("Cola", 75, 2);

        System.out.println("=== Normal purchase with change ===");
        System.out.println("State: " + vm.getStateName());
        vm.insertCoin(100);
        System.out.println("State: " + vm.getStateName());
        vm.selectProduct();
        System.out.println("State: " + vm.getStateName());

        System.out.println("\n=== Last item → out of stock ===");
        vm.insertCoin(75);
        vm.selectProduct();
        System.out.println("State: " + vm.getStateName());

        System.out.println("\n=== Try to buy when out of stock ===");
        vm.insertCoin(75);

        System.out.println("\n=== Maintenance cycle ===");
        vm.enterMaintenance();
        System.out.println("State: " + vm.getStateName());
        vm.insertCoin(50);  // rejected in maintenance
        vm.exitMaintenance();
        System.out.println("State: " + vm.getStateName());

        System.out.println("\n=== Cancel with coin ===");
        VendingMachineContext vm2 = new VendingMachineContext("Chips", 50, 5);
        vm2.insertCoin(100);
        vm2.cancel();  // returns 100 cents
        System.out.println("State: " + vm2.getStateName());

        // Adding a 7th state (CARD_READER_ERROR):
        //   1. Create CardReaderErrorState extends AbstractVendingState
        //   2. Override only the methods legal from that state
        //   3. Update only the states that transition INTO it (e.g. CoinInsertedState)
        //   All other 6 state classes: unchanged.
        System.out.println("\n=== Adding states is now O(1) per affected state ===");
        System.out.println("CardReaderErrorState would extend AbstractVendingState.");
        System.out.println("Only states transitioning to it need to change.");
    }
}
