/**
 * PRESSURE: the producer silently changes what the "amount" field means -- from
 * integer cents to a decimal dollar amount -- without renaming the field or signaling
 * the change in any way. The consumer's code never changed and still assumes "amount"
 * is an integer number of cents, so it silently computes the wrong charge.
 */
public class Main {

    // Old consumer logic, unchanged: assumes "amount" is always an integer number of cents.
    static int chargeInCents(PaymentEvent event) {
        return (int) event.get("amount");
    }

    public static void main(String[] args) {
        // Producer v1 (original contract): amount is an integer number of cents.
        PaymentEvent v1Event = new PaymentEvent();
        v1Event.set("orderId", "order-501");
        v1Event.set("amount", 4999); // $49.99, expressed as 4999 cents

        int chargedV1 = chargeInCents(v1Event);
        System.out.println("v1 event: amount field = 4999 (int, cents)");
        System.out.println("Consumer computed charge: " + chargedV1 + " cents (expected 4999 cents)");
        System.out.println();

        // Producer v2 (silent breaking change): "amount" now means dollars, not cents,
        // and it's a double -- but the field name and the consumer code never changed.
        PaymentEvent v2Event = new PaymentEvent();
        v2Event.set("orderId", "order-502");
        v2Event.set("amount", 49.99); // $49.99, expressed as 49.99 dollars

        try {
            int chargedV2 = chargeInCents(v2Event);
            System.out.println("v2 event: amount field = 49.99 (double, dollars)");
            System.out.println("Consumer computed charge: " + chargedV2 + " cents (WRONG -- ClassCastException avoided only because this line never runs)");
        } catch (ClassCastException e) {
            System.out.println("v2 event: amount field = 49.99 (double, dollars)");
            System.out.println("Consumer threw: " + e.getClass().getSimpleName() + " -- cannot cast Double to Integer");
            System.out.println("The consumer never changed. The producer changed what the same field name means.");
        }
    }
}
