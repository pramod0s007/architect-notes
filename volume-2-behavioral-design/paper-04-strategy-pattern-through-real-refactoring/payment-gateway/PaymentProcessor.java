/**
 * Stable caller — delegates to whatever {@link PaymentStrategy} is composed in.
 * Switching providers at runtime requires no changes here.
 */
public final class PaymentProcessor {

    private PaymentStrategy strategy;

    public PaymentProcessor(PaymentStrategy strategy) {
        if (strategy == null) {
            throw new IllegalArgumentException("strategy must not be null");
        }
        this.strategy = strategy;
    }

    /** Swap providers at runtime (e.g., primary fails, fall back to secondary). */
    public void setStrategy(PaymentStrategy strategy) {
        if (strategy == null) {
            throw new IllegalArgumentException("strategy must not be null");
        }
        this.strategy = strategy;
    }

    public String processPayment(double amount, String currency) {
        System.out.printf("--- Processing payment via %s ---%n", strategy.providerName());
        return strategy.pay(amount, currency);
    }

    public void processRefund(String transactionId, double amount) {
        System.out.printf("--- Processing refund via %s ---%n", strategy.providerName());
        strategy.refund(transactionId, amount);
    }
}
