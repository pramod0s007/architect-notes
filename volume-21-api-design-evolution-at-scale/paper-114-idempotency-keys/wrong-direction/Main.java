/**
 * Simulates two requests carrying the exact same idempotency key arriving
 * close enough together that both complete their "has this key been seen?"
 * check before either one finishes recording it -- e.g. a client's own
 * automatic retry firing while the first attempt's response is still in
 * flight, so both requests are genuinely concurrent from the server's
 * point of view. The steps are interleaved explicitly here (not with real
 * threads) so the race is deterministic and reproducible on every run,
 * instead of depending on scheduler timing.
 */
public class Main {
    public static void main(String[] args) {
        PaymentService service = new PaymentService();
        String idempotencyKey = "idem-key-abc123";

        System.out.println("=== Request A arrives with idempotency key 'idem-key-abc123' ===");
        boolean aAlreadySeen = service.hasBeenSeen(idempotencyKey);
        System.out.println("Request A checks hasBeenSeen(key): " + aAlreadySeen);

        System.out.println();
        System.out.println("=== Before Request A finishes, Request B arrives with the SAME idempotency key "
                + "(client's own retry, fired because A's response was slow) ===");
        boolean bAlreadySeen = service.hasBeenSeen(idempotencyKey);
        System.out.println("Request B checks hasBeenSeen(key): " + bAlreadySeen);

        System.out.println();
        System.out.println("=== Both checks already completed before either request recorded the key -- "
                + "both now proceed to charge ===");
        service.charge("ORD-1", 49.99);
        service.markSeen(idempotencyKey);
        System.out.println("Request A charges and marks the key seen.");
        service.charge("ORD-1", 49.99);
        service.markSeen(idempotencyKey);
        System.out.println("Request B charges and marks the key seen (redundant, but the damage is already done).");

        System.out.println();
        System.out.println("Ledger for order ORD-1: " + service.ledger());
        int count = 0;
        double total = 0.0;
        for (String entry : service.ledger()) {
            if (entry.startsWith("ORD-1:")) {
                total += Double.parseDouble(entry.substring("ORD-1:".length()));
                count++;
            }
        }
        System.out.println("CONFIRMED FAILURE: order ORD-1 was charged " + count + " times for a total of "
                + String.format("%.2f", total) + " despite both requests carrying the identical idempotency key "
                + "-- the idempotency check was real, but \"check, then act\" is two steps, and both requests "
                + "got through the check before either one got through the act.");
    }
}
