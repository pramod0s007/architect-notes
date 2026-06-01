/**
 * Run: javac *.java && java Main
 *
 * Demonstrates switching payment providers at runtime without touching
 * PaymentProcessor — the Strategy pattern's core promise.
 */
public final class Main {

    public static void main(String[] args) {

        // --- Scenario 1: US market checkout uses Stripe ---
        PaymentProcessor processor = new PaymentProcessor(
                new StripeStrategy("sk_live_acme_key_12345678"));

        String stripeTx = processor.processPayment(99.99, "USD");
        processor.processRefund(stripeTx, 99.99);

        System.out.println();

        // --- Scenario 2: European checkout uses PayPal ---
        processor.setStrategy(new PayPalStrategy("ACME_CLIENT_ID", "ACME_SECRET"));

        String paypalTx = processor.processPayment(149.00, "EUR");
        processor.processRefund(paypalTx, 50.00); // partial refund

        System.out.println();

        // --- Scenario 3: India market checkout uses Razorpay ---
        processor.setStrategy(new RazorpayStrategy("rzp_live_acme", "rzp_secret_acme"));

        String razorpayTx = processor.processPayment(2500.00, "INR");
        processor.processRefund(razorpayTx, 2500.00);
    }
}
