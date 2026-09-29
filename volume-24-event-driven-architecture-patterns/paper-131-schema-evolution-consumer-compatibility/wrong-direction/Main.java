/**
 * WRONG DIRECTION: instead of the crash, defensively widen the field read to accept
 * any Number type and truncate it to an int. This "fixes" the crash -- no exception is
 * ever thrown again -- but it has no idea the producer changed the field's UNIT from
 * cents to dollars, so it silently truncates 49.99 dollars into 49 (treated as 49
 * cents), a charge 100x smaller than it should be, with no error or signal at all.
 */
public class Main {

    // "Defensive" consumer logic: accepts any Number, truncates to int, never crashes.
    static int chargeInCents(PaymentEvent event) {
        Object raw = event.get("amount");
        if (raw instanceof Number) {
            return ((Number) raw).intValue();
        }
        return 0;
    }

    public static void main(String[] args) {
        PaymentEvent v1Event = new PaymentEvent();
        v1Event.set("orderId", "order-501");
        v1Event.set("amount", 4999); // $49.99, expressed as 4999 cents

        int chargedV1 = chargeInCents(v1Event);
        System.out.println("v1 event: amount field = 4999 (int, cents)");
        System.out.println("Consumer computed charge: " + chargedV1 + " cents (expected 4999 cents)");
        System.out.println();

        PaymentEvent v2Event = new PaymentEvent();
        v2Event.set("orderId", "order-502");
        v2Event.set("amount", 49.99); // $49.99, expressed as 49.99 dollars

        int chargedV2 = chargeInCents(v2Event);
        System.out.println("v2 event: amount field = 49.99 (double, dollars)");
        System.out.println("Consumer computed charge: " + chargedV2 + " cents (expected 4999 cents -- WRONG, no exception, no signal at all)");
        System.out.println("Widening the field read to accept any Number stopped the crash,");
        System.out.println("but it still has no idea the unit changed from cents to dollars.");
    }
}
