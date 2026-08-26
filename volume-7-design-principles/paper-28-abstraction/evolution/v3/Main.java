public class Main {
    public static void main(String[] args) {
        // The bloated interface forces a runtime failure for an operation Stripe never had.
        PaymentGatewayBloated bloatedStripe = new StripeGatewayBloated();
        try {
            bloatedStripe.getExchangeRate("USD", "EUR");
        } catch (UnsupportedOperationException e) {
            System.out.println("Bloated interface failure at runtime: " + e.getMessage());
        }

        // The split interfaces make this a compile-time fact instead.
        StripeGateway stripe = new StripeGateway();
        System.out.println("Stripe charge: " + stripe.charge(100.0));
        stripe.refundPartial("txn-1", 20.0, "customer request");
        System.out.println("Stripe recurring: " + stripe.chargeRecurring(9.99, "plan-monthly"));

        CryptoGateway crypto = new CryptoGateway();
        System.out.println("Crypto charge: " + crypto.charge(0.05));
        // crypto.refundPartial(...) — does not compile: CryptoGateway never claimed RefundCapable.
        // crypto.chargeRecurring(...) — does not compile: CryptoGateway never claimed RecurringCapable.
        System.out.println("Crypto correctly has no refund/recurring methods to call.");
    }
}
