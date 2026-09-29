import java.util.HashMap;
import java.util.Map;

/**
 * PRESSURE: Nova's order-placement path writes to two separate stores -- the Orders store and the
 * Inventory store -- as two direct, uncoordinated writes. The order write succeeds. The inventory
 * write fails (modeled here as a simulated downstream fault after the order write has already
 * committed). Nothing retries the failed inventory write and nothing rolls back the order write,
 * because the two writes were never coordinated as one unit in the first place.
 */
public class Main {
    public static void main(String[] args) {
        Map<String, Integer> inventory = new HashMap<>();
        inventory.put("SKU-NOVA-100", 50);

        Map<String, String> orders = new HashMap<>();
        int orderedUnits = 5;
        boolean simulatedInventoryWriteFault = true;

        System.out.println("-- Nova order placement, two uncoordinated direct writes --");
        System.out.println("Starting inventory for SKU-NOVA-100: " + inventory.get("SKU-NOVA-100") + " units");
        System.out.println("Order request: " + orderedUnits + " units of SKU-NOVA-100");
        System.out.println();

        // Write 1: Orders store.
        orders.put("ORDER-1001", "PLACED");
        System.out.println("Write 1 (Orders store): ORDER-1001 = PLACED -- committed");

        // Write 2: Inventory store, direct and uncoordinated with write 1.
        System.out.println("Write 2 (Inventory store): attempting to decrement " + orderedUnits + " units...");
        if (simulatedInventoryWriteFault) {
            System.out.println("Write 2 (Inventory store): simulated fault -- write did not commit");
        } else {
            inventory.put("SKU-NOVA-100", inventory.get("SKU-NOVA-100") - orderedUnits);
        }
        System.out.println();

        System.out.println("Result:");
        System.out.println("  Orders store: ORDER-1001 = " + orders.get("ORDER-1001"));
        System.out.println("  Inventory store: SKU-NOVA-100 = " + inventory.get("SKU-NOVA-100") + " units (expected 45)");
        System.out.println();
        System.out.println("Nothing coordinated write 1 and write 2 as one unit, and nothing retried write 2 after");
        System.out.println("it failed. ORDER-1001 is permanently recorded as PLACED while the inventory it was");
        System.out.println("supposed to consume was never decremented -- a permanent inconsistency between the");
        System.out.println("two stores that nothing in this path detects or corrects.");
    }
}
