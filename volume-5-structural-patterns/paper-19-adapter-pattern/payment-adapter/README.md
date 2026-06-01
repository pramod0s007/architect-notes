# Payment Adapter — Adapter Pattern (Anti-Corruption Layer)

## What This Demonstrates

Adapter Pattern applied as an anti-corruption layer between incompatible payment
SDKs and a payment service. `StripeSDK` and `PayPalSDK` have fundamentally
different API models. `StripePaymentAdapter` and `PayPalPaymentAdapter` each
implement `PaymentGateway` and translate every SDK call and result to the
application's own types. `PaymentService` imports zero SDK classes and runs
identically over either provider.

**Pressure: Interface Incompatibility** — Stripe and PayPal are incompatible not
only in method names but in their conceptual models: Stripe is cents-based and
charges in one call; PayPal separates order creation from capture, and refunds
target a `captureId` rather than the `orderId` that was returned to the caller.
Calling either SDK directly from `PaymentService` embeds provider semantics into
business logic. Adding a third provider (Braintree, Adyen) would require
modifying the service.

## Translation Complexity

```
Stripe model                               PayPal model
─────────────────────────────────────────  ────────────────────────────────────────────
Amount unit: cents (int)                   Amount unit: dollars (decimal String)
$49.99 → 4999 cents                        $49.99 → "49.99"

Charge call: createCharge(customerId,      Charge flow: createOrder(amount, currency)
             amountCents, currency)          → orderId + captureId on success
  → StripeCharge { chargeId, status }
  "succeeded" → ChargeResult.succeeded()  "COMPLETED" → ChargeResult.succeeded(orderId)

Refund call: refund(chargeId, amountCents) Refund call: refundCapture(captureId)
  → StripeRefund { refundId, status }       ← captureId, not orderId!
  "succeeded" → RefundResult.succeeded()  "COMPLETED" → RefundResult.succeeded(refundId)
```

## Class Diagram

```
<<interface>>
PaymentGateway
+ charge(customerId, amount, currency): ChargeResult
+ refund(chargeId, amount): RefundResult
        △
        |
   ─────────────────────────────────────────────────────
   |                                                    |
StripePaymentAdapter                      PayPalPaymentAdapter
- stripeSDK: StripeSDK                    - paypalSDK: PayPalSDK
                                          - captureIdByTransactionId: Map<String,String>
charge():                                 charge():
  amount × 100 → cents                     createOrder(amount, currency)
  stripeSDK.createCharge(...)              orderId → transactionId
  "succeeded" → ChargeResult               captureId stored in map
refund():                                 refund():
  amount × 100 → cents                     look up captureId by transactionId
  stripeSDK.refund(chargeId, cents)        paypalSDK.refundCapture(captureId)
  "succeeded" → RefundResult               "COMPLETED" → RefundResult

StripeSDK                                 PayPalSDK
createCharge(cust,cents,currency)        createOrder(amount,currency)
  → StripeCharge{chargeId,status}          → PayPalOrderResponse{orderId,captureId,status}
refund(chargeId,cents)                   refundCapture(captureId)
  → StripeRefund{refundId,status}          → PayPalRefundResponse{refundId,status}

PaymentService                           ChargeResult / RefundResult
- gateway: PaymentGateway                + succeeded(id): ChargeResult
completePurchase(cust,amount,currency)   + failed(message): ChargeResult
  → gateway.charge(...)                  - success: boolean
  → log result                           - transactionId/refundId: String
processRefund(txId, amount)              - errorMessage: String
  → gateway.refund(...)
```

## Sequence Diagram — PayPal charge and refund

```
PaymentService       PayPalPaymentAdapter         PayPalSDK
      │                       │                       │
      │ charge("custXYZ",     │                       │
      │         49.99, "USD") │                       │
      │──────────────────────>│                       │
      │                       │ createOrder(49.99,"USD")
      │                       │──────────────────────>│
      │                       │<────────────────────── PayPalOrderResponse
      │                       │   {orderId:"PAYID-...",│
      │                       │    captureId:"CAP-...",│
      │                       │    status:"COMPLETED"} │
      │                       │ captureIdByTransactionId
      │                       │   .put(orderId, captureId)
      │<──────────────────────│ ChargeResult.succeeded(orderId)
      │                       │                       │
      │ refund(orderId, 49.99)│                       │
      │──────────────────────>│                       │
      │                       │ captureId = map.get(orderId)
      │                       │ refundCapture(captureId)
      │                       │──────────────────────>│
      │                       │<────────────────────── PayPalRefundResponse
      │<──────────────────────│ RefundResult.succeeded(refundId)
```

## Key Rule: Adapters Translate, Never Transform

```
Adapter responsibility:                Business logic responsibility:
─────────────────────────────────────  ────────────────────────────────────────
amount × 100 (dollars → cents)         fraud check before charge
orderId ↔ captureId mapping            approval workflow
"succeeded"/"COMPLETED" → boolean      loyalty points on payment
SDK exception → application exception  retry policy for transient failures
```

Business rules belong in `PaymentService`, not in adapters. If the adapter
performs a fraud check or applies a discount, swapping providers becomes
impossible because both adapters would duplicate the business logic. The adapter
is a pure translation layer.

## Design Decisions

- **`PayPalPaymentAdapter` maintains a `captureId → orderId` Map internally** —
  PayPal returns an `orderId` and a `captureId` at charge time, but refunds require
  the `captureId`. The adapter returns `orderId` as the transaction ID (so callers
  have a consistent reference), and stores the `captureId` keyed by `orderId`
  for refund lookups. This mapping is a translation detail, not a business rule.
  `PaymentService` never sees it.
- **Dollar-to-cents conversion in `StripePaymentAdapter`** — `PaymentService`
  always speaks dollars (the application's currency unit). Stripe's cents
  convention is a Stripe detail. The conversion lives in one place, not in every
  call site.
- **`ChargeResult` and `RefundResult` use static factory methods** —
  `ChargeResult.succeeded(id)` and `ChargeResult.failed(msg)` are readable and
  avoid nullable fields. Both adapters produce the same normalized result types;
  `PaymentService` reads them with no provider-specific branches.
- **`PaymentService` is constructor-injected with `PaymentGateway`** — the
  service can be tested with a mock or stub gateway without any SDK involved.
  Switching from Stripe to PayPal in production requires changing one construction
  site.

## How to Run

```bash
cd volume-5-structural-patterns/paper-19-adapter-pattern/payment-adapter
javac *.java && java Main
```

Expected output (abbreviated):

```
=== Stripe Payment Gateway ===
  [PaymentService] Charging customer=cus_Abc123 amount=49.99 USD
  [Stripe SDK] POST /v1/charges amount=4999 cents
  [PaymentService] Charge succeeded. Transaction: ch_...
  [Stripe SDK] POST /v1/refunds charge=ch_...
  [PaymentService] Refund succeeded. Refund ID: re_...

=== PayPal Payment Gateway ===
  [PaymentService] Charging customer=customer_XYZ amount=49.99 USD
  [PayPal SDK] POST /v2/checkout/orders amount=49.99 USD
  [PaymentService] Charge succeeded. Transaction: PAYID-...
  [PayPal SDK] POST /v2/payments/captures/CAP-.../refund
  [PaymentService] Refund succeeded. Refund ID: REF-...
```

## When to Apply

- Two or more third-party systems have incompatible APIs representing the same
  domain concept (payment, storage, notifications).
- Your business logic must be provider-agnostic and independently testable.
- The translation between SDKs is non-trivial (model mapping, unit conversion,
  state tracking) — isolating it in one place protects every call site.

## When NOT to Apply

- Only one provider exists and no second provider is planned — the adapter adds
  indirection with no benefit.
- The SDKs are so different that a thin adapter cannot reconcile them without
  leaking provider specifics — in that case a richer anti-corruption layer with
  its own domain model may be needed.
