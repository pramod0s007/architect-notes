import java.util.Arrays;
import java.util.List;

/** Demos the LSP violation in the payment hierarchy, then the split-interface fix. */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== VIOLATION: PaymentProcessor ===");
        PaymentProcessor instant   = new InstantPayment();
        PaymentProcessor scheduled = new ScheduledPayment();
        PaymentProcessor crypto    = new CryptoPayment();

        instant.process(120.00,   "alice@example.com");
        scheduled.process(450.00, "bob@example.com");
        crypto.process(0.005,     "0xABCDEF");

        PaymentService service = new PaymentService();
        service.processRefund(instant,   "INS-001");   // works
        service.processRefund(scheduled, "SCH-001");   // works
        service.processRefund(crypto,    "CRY-001");   // instanceof guard fires

        System.out.println("Direct refund on crypto (no guard):");
        try {
            crypto.refund("CRY-001");
        } catch (UnsupportedOperationException e) {
            System.out.println("  Runtime exception: " + e.getMessage());
        }

        System.out.println();
        System.out.println("=== FIX: RefundablePayment / IrrefundablePayment ===");
        RefundablePayment fixedInstant   = new InstantPaymentFixed();
        RefundablePayment fixedScheduled = new ScheduledPaymentFixed();
        IrrefundablePayment fixedCrypto  = new CryptoPaymentFixed();

        fixedInstant.process(200.00,   "carol@example.com");
        fixedScheduled.process(75.50,  "dave@example.com");
        fixedCrypto.process(0.01,      "0x123456");

        // No instanceof — compiler guarantees every item supports refund
        PaymentServiceFixed fixed = new PaymentServiceFixed();
        fixed.refundAll(Arrays.asList(fixedInstant, fixedScheduled), "TX-BATCH-001");

        // crypto cannot be added to List<RefundablePayment> — compiler rejects it
        System.out.println("Crypto status: " + fixedCrypto.getStatus("TX-999"));
    }
}
