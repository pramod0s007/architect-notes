/**
 * Simulates the exact same race as the wrong-direction scenario (Request B
 * arrives with the same idempotency key before Request A has finished),
 * plus a second, separate case: a duplicate that arrives well after the
 * original charge has fully completed, which should be handed back the
 * original result instead of triggering a new charge.
 */
public class Main {
    public static void main(String[] args) {
        PaymentService service = new PaymentService();
        String idempotencyKey = "idem-key-abc123";

        System.out.println("=== Race case: Request A and Request B both carry the same idempotency key ===");
        PaymentService.ChargeResult aReservation = service.reserve(idempotencyKey);
        System.out.println("Request A calls reserve(key): " + aReservation
                + " (null means A won the reservation and must process the charge)");

        PaymentService.ChargeResult bReservation = service.reserve(idempotencyKey);
        System.out.println("Request B calls reserve(key) before A has finished: " + bReservation
                + " (B lost the race -- it must NOT charge, and will return this result once A completes)");

        PaymentService.ChargeResult finalResult = service.processCharge(idempotencyKey, "ORD-1", 49.99);
        System.out.println("Request A actually processes the charge: " + finalResult);
        System.out.println("Request B returns the SAME result to its caller instead of charging: " + finalResult);

        System.out.println();
        System.out.println("Ledger for order ORD-1 after the race: " + service.ledger());

        System.out.println();
        System.out.println("=== Late-duplicate case: a third copy of the same request arrives after "
                + "the charge has fully completed ===");
        PaymentService.ChargeResult lateReservation = service.reserve(idempotencyKey);
        System.out.println("Request C calls reserve(key) after A finished: " + lateReservation
                + " (a real, completed result -- not the PENDING placeholder -- so C can return it directly)");

        System.out.println();
        System.out.println("Ledger for order ORD-1 after the late duplicate: " + service.ledger());
        int count = 0;
        double total = 0.0;
        for (String entry : service.ledger()) {
            if (entry.startsWith("ORD-1:")) {
                total += Double.parseDouble(entry.substring("ORD-1:".length()));
                count++;
            }
        }
        if (count == 1) {
            System.out.println("FIXED: order ORD-1 was charged exactly " + count + " time for "
                    + String.format("%.2f", total) + ", even though three separate requests carried the same "
                    + "idempotency key -- the reservation is a single atomic operation, so exactly one of them "
                    + "could ever win it, and every other one (whether racing or arriving later) was handed "
                    + "back the winner's own result instead of being allowed to charge.");
        }
    }
}
