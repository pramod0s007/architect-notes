import java.util.HashMap;
import java.util.Map;

/**
 * WRONG DIRECTION: the order write and an outbox event are modeled as committed together in one
 * local transaction -- the code itself does not enforce that transaction, only prints as if it
 * were one. Given a durable outbox row and a relay that keeps retrying, the inventory-decrement
 * intent remains available for delivery rather than being silently dropped, because it was
 * recorded alongside the order write. A separate consumer reads the outbox event and applies the
 * inventory decrement. The gap this scenario has not addressed: the consumer applies the
 * decrement, then a simulated crash occurs before the event is marked processed, so the same
 * event is redelivered and reapplied a second time.
 */
public class Main {
    public static void main(String[] args) {
        Map<String, Integer> inventory = new HashMap<>();
        inventory.put("SKU-NOVA-100", 50);

        Map<String, String> orders = new HashMap<>();
        Map<String, String> outbox = new HashMap<>();
        int orderedUnits = 5;

        System.out.println("-- Nova order placement, outbox pattern without idempotent consumption --");
        System.out.println("Starting inventory for SKU-NOVA-100: " + inventory.get("SKU-NOVA-100") + " units");
        System.out.println("Order request: " + orderedUnits + " units of SKU-NOVA-100");
        System.out.println();

        // Coordinated write: order + outbox event modeled as one local transaction.
        orders.put("ORDER-1001", "PLACED");
        outbox.put("EVENT-1", "decrement SKU-NOVA-100 by " + orderedUnits);
        System.out.println("Coordinated write (Orders store + outbox, modeled as one local transaction):");
        System.out.println("ORDER-1001 = PLACED,");
        System.out.println("EVENT-1 = \"" + outbox.get("EVENT-1") + "\" -- modeled as committed together in one local transaction");
        System.out.println();

        // First delivery of EVENT-1: consumer applies the decrement.
        System.out.println("Consumer delivery 1 of EVENT-1: applying decrement...");
        inventory.put("SKU-NOVA-100", inventory.get("SKU-NOVA-100") - orderedUnits);
        System.out.println("  Inventory after delivery 1: " + inventory.get("SKU-NOVA-100") + " units");
        System.out.println("  Simulated crash before EVENT-1 is marked processed -- acknowledgment is lost");
        System.out.println();

        // Redelivery of EVENT-1, because it was never marked processed.
        System.out.println("Consumer delivery 2 of EVENT-1 (redelivered, since it was never marked processed):");
        System.out.println("  applying decrement again...");
        inventory.put("SKU-NOVA-100", inventory.get("SKU-NOVA-100") - orderedUnits);
        System.out.println("  Inventory after delivery 2: " + inventory.get("SKU-NOVA-100") + " units (expected 45)");
        System.out.println();

        System.out.println("Result:");
        System.out.println("  Orders store: ORDER-1001 = " + orders.get("ORDER-1001"));
        System.out.println("  Inventory store: SKU-NOVA-100 = " + inventory.get("SKU-NOVA-100") + " units");
        System.out.println();
        System.out.println("The delivery gap from the direct-write scenario is closed -- EVENT-1 was never lost.");
        System.out.println("A different gap is open: the outbox's own at-least-once delivery guarantee means");
        System.out.println("EVENT-1 can be, and here was, delivered twice, and nothing in this consumer checked");
        System.out.println("whether it had already applied EVENT-1 before applying it again.");
    }
}
