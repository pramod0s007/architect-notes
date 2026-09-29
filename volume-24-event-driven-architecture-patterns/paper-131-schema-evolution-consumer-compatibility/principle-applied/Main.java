/**
 * PRINCIPLE APPLIED: the producer stamps every event with an explicit schemaVersion,
 * and never changes what an already-published version's fields mean. The consumer
 * reads the version first and interprets "amount" according to that version's known,
 * fixed contract -- schemaVersion 1 is always integer cents, schemaVersion 2 is always
 * decimal dollars. Both versions are handled correctly, forever, because a version's
 * contract is a promise that's never broken after publication.
 */
public class Main {

    // Consumer logic: read schemaVersion first, then interpret "amount" per that version's contract.
    static int chargeInCents(PaymentEvent event) {
        int schemaVersion = (int) event.get("schemaVersion");
        switch (schemaVersion) {
            case 1:
                // Contract: amount is an integer number of cents.
                return (int) event.get("amount");
            case 2:
                // Contract: amount is a decimal number of dollars.
                double dollars = (double) event.get("amount");
                return (int) Math.round(dollars * 100);
            default:
                throw new IllegalStateException("Unknown schemaVersion: " + schemaVersion);
        }
    }

    public static void main(String[] args) {
        PaymentEvent v1Event = new PaymentEvent();
        v1Event.set("schemaVersion", 1);
        v1Event.set("orderId", "order-501");
        v1Event.set("amount", 4999); // schemaVersion 1 contract: integer cents

        int chargedV1 = chargeInCents(v1Event);
        System.out.println("v1 event: schemaVersion=1, amount=4999 (int, cents)");
        System.out.println("Consumer computed charge: " + chargedV1 + " cents (expected 4999 cents)");
        System.out.println();

        PaymentEvent v2Event = new PaymentEvent();
        v2Event.set("schemaVersion", 2);
        v2Event.set("orderId", "order-502");
        v2Event.set("amount", 49.99); // schemaVersion 2 contract: decimal dollars

        int chargedV2 = chargeInCents(v2Event);
        System.out.println("v2 event: schemaVersion=2, amount=49.99 (double, dollars)");
        System.out.println("Consumer computed charge: " + chargedV2 + " cents (expected 4999 cents)");
        System.out.println();

        boolean bothCorrect = chargedV1 == 4999 && chargedV2 == 4999;
        System.out.println("Both schema versions charged correctly: " + bothCorrect);
        System.out.println("A version's contract, once published, never changes what its own");
        System.out.println("fields mean -- a genuine unit change gets a new version instead.");
    }
}
