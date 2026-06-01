/**
 * Demonstrates the Adapter pattern for payment gateways.
 *
 * The same {@link PaymentService} logic runs identically over both Stripe and
 * PayPal. The adapters translate between the application's interface and each
 * SDK's incompatible types and conventions.
 */
public class Main {

    public static void main(String[] args) {

        // ── Stripe flow ───────────────────────────────────────────────────
        System.out.println("=== Stripe Payment Gateway ===");
        StripeSDK stripeSDK          = new StripeSDK();
        PaymentGateway stripeGateway = new StripePaymentAdapter(stripeSDK);
        PaymentService stripeService = new PaymentService(stripeGateway);

        String stripeTxId = stripeService.completePurchase("cus_Abc123", 49.99, "USD");
        if (stripeTxId != null) {
            System.out.println("  Transaction ID: " + stripeTxId);
            stripeService.processRefund(stripeTxId, 49.99);
        }

        // ── PayPal flow — exact same service, different adapter ───────────
        System.out.println("\n=== PayPal Payment Gateway ===");
        PayPalSDK paypalSDK          = new PayPalSDK();
        PaymentGateway paypalGateway = new PayPalPaymentAdapter(paypalSDK);
        PaymentService paypalService = new PaymentService(paypalGateway);

        String paypalTxId = paypalService.completePurchase("customer_XYZ", 49.99, "USD");
        if (paypalTxId != null) {
            System.out.println("  Transaction ID: " + paypalTxId);
            paypalService.processRefund(paypalTxId, 49.99);
        }

        // ── Multiple charges through Stripe ───────────────────────────────
        System.out.println("\n=== Multiple Stripe Charges ===");
        stripeService.completePurchase("cus_Def456", 19.00, "USD");
        stripeService.completePurchase("cus_Ghi789", 299.95, "EUR");

        // ── Multiple charges through PayPal ───────────────────────────────
        System.out.println("\n=== Multiple PayPal Charges ===");
        paypalService.completePurchase("cust_AA1", 75.00, "GBP");
        paypalService.completePurchase("cust_BB2", 12.50, "USD");

        // ── Demonstrate provider-agnostic function ────────────────────────
        System.out.println("\n=== Provider-Agnostic Charge Helper ===");
        chargeWithAnyProvider(stripeGateway, "cus_TestS", 9.99, "USD", "Stripe");
        chargeWithAnyProvider(paypalGateway, "cust_TestP", 9.99, "USD", "PayPal");
    }

    /**
     * Shows that the same code path works with any PaymentGateway implementation.
     */
    private static void chargeWithAnyProvider(
            PaymentGateway gateway, String customerId,
            double amount, String currency, String providerLabel) {

        ChargeResult result = gateway.charge(customerId, amount, currency);
        System.out.println("  [" + providerLabel + "] charge -> " + result);
    }
}
