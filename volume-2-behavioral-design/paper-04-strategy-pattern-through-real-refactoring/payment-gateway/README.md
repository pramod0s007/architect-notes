# Payment Gateway — Strategy Pattern

## What This Demonstrates

Three payment providers (PayPal, Stripe, Razorpay) behind a single
`PaymentStrategy` interface. `PaymentProcessor` never changes when a new
provider is added or an existing one is removed. `setStrategy()` enables
runtime switching — if the primary provider fails, swap to the secondary
without touching any calling code.

**Pressure: Behavior Variation** — payment logic differs per provider
(API keys, endpoint URLs, currency conventions); providers are added and
removed over time as the business expands to new markets.

## Class Diagram

```
<<interface>>
PaymentStrategy
+ pay(amount: double, currency: String): String
+ refund(transactionId: String, amount: double): void
+ providerName(): String
        △
        |
   ─────────────────────────────────────────
   |                   |                   |
PayPalStrategy    StripeStrategy    RazorpayStrategy
(clientId/secret) (apiKey)          (keyId/secret)

PaymentProcessor                              [context]
- strategy: PaymentStrategy
+ PaymentProcessor(strategy)
+ setStrategy(strategy): void                 ← runtime swap / failover
+ processPayment(amount, currency): String
+ processRefund(transactionId, amount): void
```

## Sequence / Flow

```
Client
  │
  ├─ new PaymentProcessor(new StripeStrategy("sk_live_..."))
  │
  ├─ processor.processPayment(99.99, "USD")
  │       └─ strategy.pay(99.99, "USD")     [StripeStrategy]
  │               └─ returns "stripe-txn-..."
  │
  ├─ processor.processRefund(txId, 99.99)
  │       └─ strategy.refund(txId, 99.99)   [StripeStrategy]
  │
  ├─ processor.setStrategy(new PayPalStrategy(...))  ← provider swap
  │
  └─ processor.processPayment(149.00, "EUR")
          └─ strategy.pay(149.00, "EUR")    [PayPalStrategy]
                  └─ returns "paypal-txn-..."
```

## Design Decisions

- **`setStrategy()` enables runtime failover** — primary provider fails,
  inject secondary; `PaymentProcessor` logs the provider name but never
  branches on which one it is.
- **`providerName()` on the interface** — receipts and audit logs identify
  the provider without casting or `instanceof`.
- **Both `pay` and `refund` on the same interface** — a provider that
  cannot refund must make a deliberate choice (throw or document the no-op);
  the interface forces that decision rather than letting it be silently absent.
- **Mutable strategy** — unlike the encryption example, `PaymentProcessor`
  deliberately allows mid-session provider swaps to model real-world
  failover without constructing a new processor each time.

## How to Run

```bash
cd volume-2-behavioral-design/paper-04-strategy-pattern-through-real-refactoring/payment-gateway
javac *.java && java Main
```

Expected output shows three payment flows (Stripe, PayPal, Razorpay) with
charge and refund per provider, all through the same `PaymentProcessor` instance.

## When to Apply

- Multiple external providers share the same logical operation (charge, refund)
  but differ in implementation details.
- The active provider must be switchable at runtime (failover, A/B testing,
  market-specific routing).

## When NOT to Apply

- Single payment provider with no planned expansion — the interface adds
  indirection with no benefit yet.
