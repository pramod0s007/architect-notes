import java.util.UUID;

/**
 * Concrete strategy — PayPal REST API (simulated).
 * Real implementation would call PayPal's Orders v2 API.
 */
public final class PayPalStrategy implements PaymentStrategy {

    private final String clientId;
    private final String clientSecret;

    public PayPalStrategy(String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    @Override
    public String pay(double amount, String currency) {
        // Simulate: obtain OAuth token, POST /v2/checkout/orders
        String txId = "PP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        System.out.printf("[PayPal] Charged %.2f %s via client=%s  txId=%s%n",
                amount, currency, clientId, txId);
        return txId;
    }

    @Override
    public void refund(String transactionId, double amount) {
        // Simulate: POST /v2/payments/captures/{id}/refund
        System.out.printf("[PayPal] Refunding %.2f for txId=%s%n", amount, transactionId);
    }

    @Override
    public String providerName() {
        return "PayPal";
    }
}
