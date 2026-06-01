import java.util.UUID;

/**
 * Concrete strategy — Razorpay API (simulated).
 * Razorpay is dominant in India; amounts are typically in paise (1 INR = 100 paise).
 * Real implementation would use razorpay-java SDK: Orders.create() / Payments.refund().
 */
public final class RazorpayStrategy implements PaymentStrategy {

    private final String keyId;
    private final String keySecret;

    public RazorpayStrategy(String keyId, String keySecret) {
        this.keyId = keyId;
        this.keySecret = keySecret;
    }

    @Override
    public String pay(double amount, String currency) {
        // Razorpay expects amount in smallest unit (paise for INR)
        long amountInPaise = Math.round(amount * 100);
        String txId = "pay_" + UUID.randomUUID().toString().replace("-", "").substring(0, 14);
        System.out.printf("[Razorpay] Charged %d paise (%.2f %s) via keyId=%s  txId=%s%n",
                amountInPaise, amount, currency, keyId, txId);
        return txId;
    }

    @Override
    public void refund(String transactionId, double amount) {
        // Simulate: POST /v1/payments/{id}/refund
        System.out.printf("[Razorpay] Refunding %.2f for txId=%s%n", amount, transactionId);
    }

    @Override
    public String providerName() {
        return "Razorpay";
    }
}
