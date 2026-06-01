import java.util.*;

// =============================================================================
// v2 — BEHAVIOR PRESSURE: Payment Gateway Routing
// =============================================================================
// Domain: Payment Processing
// Pressure type: BEHAVIOR — the routing algorithm itself varies per gateway
//
// Month 1: Route to PayPal only
// Month 3: Stripe added — different SDK, different retry logic, different fees
// Month 5: Razorpay added (India market) — different auth, different currency
// Month 7: Square added (US POS market) — different signature scheme
// Month 9: Adyen added (EU enterprise) — different tokenization, different endpoints
//
// SIGNAL that this is BEHAVIOR pressure (not data, not object, not rules):
//   - Each gateway has a genuinely different ROUTING ALGORITHM
//   - PayPal: OAuth2 flow, amount in cents, USD default
//   - Stripe: API key header, amount in cents, metadata map
//   - Razorpay: HMAC signature, INR conversion, webhook verification
//   - Square: version header, idempotency key, location ID required
//   - Adyen: merchant account required, shopper reference, split payments
//   The computation differs per branch — this is behavior variation.
//
// Solution preview (in v3): Strategy Pattern
// =============================================================================

public class v2_BehaviorPressure {

    static class Payment {
        private final String id;
        private final double amount;
        private final String currency;
        private final String payerId;

        public Payment(String id, double amount, String currency, String payerId) {
            this.id       = id;
            this.amount   = amount;
            this.currency = currency;
            this.payerId  = payerId;
        }
        public String getId()       { return id; }
        public double getAmount()   { return amount; }
        public String getCurrency() { return currency; }
        public String getPayerId()  { return payerId; }
    }

    static class GatewayResult {
        final boolean success;
        final String transactionId;
        final String gateway;
        GatewayResult(boolean success, String txId, String gateway) {
            this.success       = success;
            this.transactionId = txId;
            this.gateway       = gateway;
        }
        @Override public String toString() {
            return "GatewayResult{gateway=" + gateway + ", success=" + success +
                   ", txId=" + transactionId + "}";
        }
    }

    // ---------------------------------------------------------------------------
    // Month 1 — PayPal only. Correct, simple, no pressure.
    // ---------------------------------------------------------------------------
    static class PaymentGatewayRouterV1 {

        public GatewayResult route(Payment payment) {
            return routePaypal(payment);
        }

        private GatewayResult routePaypal(Payment p) {
            // PayPal: OAuth2 token, amount in cents, USD default
            System.out.println("[PayPal] OAuth2 token acquired");
            System.out.println("[PayPal] POST /v2/checkout/orders amount=" + (int)(p.getAmount() * 100) + " cents");
            return new GatewayResult(true, "PP-" + p.getId(), "paypal");
        }
    }

    // ---------------------------------------------------------------------------
    // Month 9 — Five gateways. if-else pain. BEHAVIOR PRESSURE.
    //
    // [!] Every gateway is a different computation — PayPal uses OAuth2 + cents,
    //     Razorpay uses HMAC + INR conversion, Adyen uses merchant account + splits
    // [!] Adding gateway #6 means: open this class, add another else-if, risk
    //     breaking existing branches, run the full payment test suite again
    // [!] Different gateways have different retry logic — encoding it in if-else
    //     means you can't test PayPal retry logic without Stripe's code in scope
    // [!] Business wants to A/B test gateways at runtime — impossible with if-else
    // [!] Each gateway should be deployable as a separate JAR — impossible here
    // ---------------------------------------------------------------------------
    static class PaymentGatewayRouterV2 {

        public GatewayResult route(Payment payment, String gateway) {
            if ("paypal".equals(gateway)) {
                // PayPal: OAuth2 + amount in cents + USD currency
                System.out.println("[PayPal] OAuth2 token acquired for payer=" + payment.getPayerId());
                System.out.println("[PayPal] POST /v2/checkout/orders amount=" + (int)(payment.getAmount() * 100));
                return new GatewayResult(true, "PP-" + payment.getId(), "paypal");

            } else if ("stripe".equals(gateway)) {
                // [!] Stripe: API key header + idempotency key + metadata map — different from PayPal
                // Added month 3
                System.out.println("[Stripe] Authorization: Bearer sk_live_... idempotency=" + payment.getId());
                System.out.println("[Stripe] POST /v1/payment_intents amount=" + (int)(payment.getAmount() * 100));
                return new GatewayResult(true, "pi_" + payment.getId(), "stripe");

            } else if ("razorpay".equals(gateway)) {
                // [!] Razorpay: HMAC-SHA256 signature + INR conversion + webhook verification
                // Added month 5
                double inrAmount = payment.getAmount() * 83.5; // USD → INR conversion
                System.out.println("[Razorpay] HMAC signature generated for orderId=" + payment.getId());
                System.out.println("[Razorpay] POST /v1/orders amount=" + (int)(inrAmount * 100) + " paise");
                return new GatewayResult(true, "order_" + payment.getId(), "razorpay");

            } else if ("square".equals(gateway)) {
                // [!] Square: version header + idempotency key + location ID required
                // Added month 7
                System.out.println("[Square] Square-Version: 2024-01-17");
                System.out.println("[Square] POST /v2/payments locationId=MAIN idempotency=" + payment.getId());
                return new GatewayResult(true, "SQ-" + payment.getId(), "square");

            } else if ("adyen".equals(gateway)) {
                // [!] Adyen: merchant account + shopper reference + split payment config
                // Added month 9
                System.out.println("[Adyen] merchantAccount=MyShopECOM shopperRef=" + payment.getPayerId());
                System.out.println("[Adyen] POST /v68/payments amount=" + payment.getAmount() + " " + payment.getCurrency());
                return new GatewayResult(true, "ADY-" + payment.getId(), "adyen");

            } else {
                throw new IllegalArgumentException("Unknown gateway: " + gateway);
            }
        }
    }

    // ---------------------------------------------------------------------------
    // v3 preview — Strategy Pattern applied
    //
    // WHAT CHANGED:
    //   - GatewayStrategy interface: single charge() method
    //   - PaypalGateway, StripeGateway, RazorpayGateway, SquareGateway, AdyenGateway
    //   - PaymentGatewayRouterV3 receives its strategy via constructor
    //   - Adding gateway #6 = new class only, zero changes here
    //
    // WHY Strategy (not Bucket 2 object variation):
    //   - The ALGORITHM varies (different auth schemes, different amount encoding,
    //     different currency handling) — this is genuine behavior, not a resource swap
    //   - We want to A/B test gateways at runtime: setGateway() at runtime
    //   - Each gateway strategy can have its own retry policy, timeout, circuit breaker
    // ---------------------------------------------------------------------------
    interface GatewayStrategy {
        GatewayResult charge(Payment payment);
    }

    static class PaypalGateway implements GatewayStrategy {
        @Override
        public GatewayResult charge(Payment p) {
            System.out.println("[PayPal] OAuth2 token acquired for payer=" + p.getPayerId());
            System.out.println("[PayPal] POST /v2/checkout/orders amount=" + (int)(p.getAmount() * 100));
            return new GatewayResult(true, "PP-" + p.getId(), "paypal");
        }
    }

    static class StripeGateway implements GatewayStrategy {
        @Override
        public GatewayResult charge(Payment p) {
            System.out.println("[Stripe] Bearer sk_live_... idempotency=" + p.getId());
            System.out.println("[Stripe] POST /v1/payment_intents amount=" + (int)(p.getAmount() * 100));
            return new GatewayResult(true, "pi_" + p.getId(), "stripe");
        }
    }

    static class RazorpayGateway implements GatewayStrategy {
        private static final double USD_TO_INR = 83.5;
        @Override
        public GatewayResult charge(Payment p) {
            double paise = p.getAmount() * USD_TO_INR * 100;
            System.out.println("[Razorpay] HMAC signature for orderId=" + p.getId());
            System.out.println("[Razorpay] POST /v1/orders amount=" + (int)paise + " paise");
            return new GatewayResult(true, "order_" + p.getId(), "razorpay");
        }
    }

    static class SquareGateway implements GatewayStrategy {
        @Override
        public GatewayResult charge(Payment p) {
            System.out.println("[Square] Square-Version: 2024-01-17 idempotency=" + p.getId());
            System.out.println("[Square] POST /v2/payments locationId=MAIN");
            return new GatewayResult(true, "SQ-" + p.getId(), "square");
        }
    }

    static class AdyenGateway implements GatewayStrategy {
        @Override
        public GatewayResult charge(Payment p) {
            System.out.println("[Adyen] merchantAccount=MyShopECOM shopperRef=" + p.getPayerId());
            System.out.println("[Adyen] POST /v68/payments amount=" + p.getAmount());
            return new GatewayResult(true, "ADY-" + p.getId(), "adyen");
        }
    }

    static class PaymentGatewayRouterV3 {
        private GatewayStrategy strategy;

        public PaymentGatewayRouterV3(GatewayStrategy strategy) {
            this.strategy = strategy;
        }

        // A/B testing: swap strategy at runtime
        public void setStrategy(GatewayStrategy strategy) {
            this.strategy = strategy;
        }

        // No if-else — delegates entirely to the strategy
        public GatewayResult route(Payment payment) {
            return strategy.charge(payment);
        }
    }

    public static void main(String[] args) {
        Payment p = new Payment("TXN-123", 99.99, "USD", "user-42");

        // Month 1: v1 — single gateway
        System.out.println("=== v1 (PayPal only) ===");
        System.out.println(new PaymentGatewayRouterV1().route(p));

        // Month 9: v2 — if-else pain
        System.out.println("\n=== v2 (if-else, 5 gateways) ===");
        PaymentGatewayRouterV2 v2 = new PaymentGatewayRouterV2();
        System.out.println(v2.route(p, "paypal"));
        System.out.println(v2.route(p, "adyen"));

        // Refactored: v3 — strategy injected
        System.out.println("\n=== v3 (Stripe via Strategy) ===");
        PaymentGatewayRouterV3 v3 = new PaymentGatewayRouterV3(new StripeGateway());
        System.out.println(v3.route(p));

        // A/B test: swap to Razorpay at runtime
        System.out.println("\n=== v3 (Razorpay A/B swap) ===");
        v3.setStrategy(new RazorpayGateway());
        System.out.println(v3.route(p));
    }
}
