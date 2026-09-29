import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * PRINCIPLE APPLIED: the same outbox pattern from the wrong-direction scenario -- order write and
 * outbox event modeled as committed together in one local transaction -- paired with an idempotent
 * consumer that tracks which event IDs it has already applied. The same redelivery that caused a
 * double decrement in the wrong-direction scenario still happens here (the modeled at-least-once
 * redelivery behavior is unchanged), but the consumer checks its own processed-event-ID record
 * before applying an event, so the redelivered copy is recognized and skipped instead of
 * reapplied. This simplified consumer applies the decrement and records the event ID as two
 * separate statements rather than one atomic operation -- a real consumer needs those two steps
 * committed together (see the paper's Cons section for why).
 */
public class Main {
    public static void main(String[] args) {
        Map<String, Integer> inventory = new HashMap<>();
        inventory.put("SKU-NOVA-100", 50);

        Map<String, String> orders = new HashMap<>();
        Map<String, String> outbox = new HashMap<>();
        Set<String> processedEventIds = new HashSet<>();
        int orderedUnits = 5;

        System.out.println("-- Nova order placement, outbox pattern + idempotent consumer --");
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

        // First delivery of EVENT-1: consumer checks processedEventIds, applies, records.
        System.out.println("Consumer delivery 1 of EVENT-1: checking processed-event-ID record...");
        if (!processedEventIds.contains("EVENT-1")) {
            System.out.println("  EVENT-1 not previously processed -- applying decrement...");
            inventory.put("SKU-NOVA-100", inventory.get("SKU-NOVA-100") - orderedUnits);
            processedEventIds.add("EVENT-1");
            System.out.println("  Inventory after delivery 1: " + inventory.get("SKU-NOVA-100") + " units");
            System.out.println("  EVENT-1 recorded as processed");
        }
        System.out.println("  Simulated crash before the outbox's own delivery acknowledgment is recorded --");
        System.out.println("  the outbox does not know EVENT-1 was already delivered, so it will redeliver it");
        System.out.println();

        // Redelivery of EVENT-1: consumer checks processedEventIds again, finds it already applied, skips.
        System.out.println("Consumer delivery 2 of EVENT-1 (redelivered by the outbox, same as before):");
        System.out.println("  checking processed-event-ID record...");
        if (!processedEventIds.contains("EVENT-1")) {
            inventory.put("SKU-NOVA-100", inventory.get("SKU-NOVA-100") - orderedUnits);
            processedEventIds.add("EVENT-1");
        } else {
            System.out.println("  EVENT-1 already in the processed-event-ID record -- skipped, not reapplied");
        }
        System.out.println("  Inventory after delivery 2: " + inventory.get("SKU-NOVA-100") + " units (expected 45)");
        System.out.println();

        System.out.println("Result:");
        System.out.println("  Orders store: ORDER-1001 = " + orders.get("ORDER-1001"));
        System.out.println("  Inventory store: SKU-NOVA-100 = " + inventory.get("SKU-NOVA-100") + " units");
        System.out.println();
        System.out.println("The outbox still redelivers EVENT-1, exactly as it did in the wrong-direction");
        System.out.println("scenario -- that at-least-once behavior is unchanged. What's different is that the");
        System.out.println("consumer's own processed-event-ID record recognizes the redelivered copy and skips");
        System.out.println("reapplying it, so in this sequential trace the decrement is not repeated. A production");
        System.out.println("consumer additionally needs the decrement and this processed-event-ID record to commit");
        System.out.println("together, atomically, so a crash between the two steps can't reopen this same gap.");
    }
}
