import java.util.UUID;

/**
 * Concrete strategy — Stripe API (simulated).
 * Real implementation would use stripe-java SDK: Charge.create() / Refund.create().
 */
public final class StripeStrategy implements PaymentStrategy {

    private final String apiKey;

    public StripeStrategy(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public String pay(double amount, String currency) {
        // Simulate: convert to smallest currency unit, call PaymentIntent
        long amountInCents = Math.round(amount * 100);
        String txId = "ch_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        System.out.printf("[Stripe] Charged %d %s (cents) via key=%s  txId=%s%n",
                amountInCents, currency.toLowerCase(), masked(apiKey), txId);
        return txId;
    }

    @Override
    public void refund(String transactionId, double amount) {
        // Simulate: POST /v1/refunds
        System.out.printf("[Stripe] Refunding %.2f for txId=%s%n", amount, transactionId);
    }

    @Override
    public String providerName() {
        return "Stripe";
    }

    private String masked(String key) {
        return key.length() > 8 ? key.substring(0, 8) + "****" : "****";
    }
}
