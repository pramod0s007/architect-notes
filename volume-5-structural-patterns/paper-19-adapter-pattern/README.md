# Adapter Pattern — Making Incompatible Interfaces Work Together

**Pattern:** Adapter Pattern

---

## Read the Full Article on Medium

_Article coming soon on Medium._

This repository focuses on runnable code examples. The white paper covers theory, war stories, and architectural reasoning in depth.

---
## What This Paper Is About

Adapter Pattern translates one interface into another without modifying either. The target interface is what your system uses. The adaptee is the external SDK, legacy system, or third-party library with an incompatible API.

The pressure: **interface incompatibility**. You cannot modify either side. The adapter owns the translation.

## The Anti-Corruption Layer

When a system integrates with many external services, individual adapters evolve into a boundary pattern: the **Anti-Corruption Layer (ACL)**.

Everything outside the ACL speaks the external service's language. Everything inside speaks your domain's language. The ACL translates at the boundary.

```
Your Domain Model  ←  CustomerAdapter  ←  CustomerRecord (mainframe)
Your Domain Model  ←  PaymentAdapter   ←  StripeSDK / PayPalSDK
Your Domain Model  ←  StorageAdapter   ←  S3SDK / AzureSDK
```

**Benefits:** New external integration = new adapter at the boundary. Domain model stays clean. Engineers working on core business logic never import external SDK types.

## The Adapter Rule

**Adapters translate. They do not transform.**

Field name mapping and type conversion belong in adapters. Business rules do not. When an adapter contains a business rule, that rule is invisible to your domain, untestable in isolation, and will silently diverge as requirements change.

```java
// Wrong — business logic in adapter
class StripeAdapter implements PaymentGateway {
    public PaymentResult charge(PaymentRequest request) {
        if (request.getAmount() > 10000 && !request.hasApproval()) // WRONG
            throw new PolicyException("High-value charge requires approval");
        return stripe.charge(...);
    }
}

// Right — adapter translates only
class StripeAdapter implements PaymentGateway {
    public PaymentResult charge(PaymentRequest request) {
        StripeChargeRequest req = translate(request);  // only translation
        StripeChargeResponse resp = stripe.charge(req);
        return translate(resp);
    }
}
```

## Object Adapter vs Class Adapter

**Object Adapter** (preferred): holds adaptee as a field. Works with concrete classes. No inheritance required.

**Class Adapter**: extends the adaptee. Requires adaptee to be a class. Single inheritance in Java makes this impractical in most cases. Avoid.

## Adapter vs Facade vs Decorator

| Pattern | Changes interface? | Purpose |
|---------|-------------------|---------|
| Adapter | Yes | Translate incompatible interface |
| Facade | Yes (simplifies) | Hide complexity of a subsystem |
| Decorator | No | Add behavior, same interface |

## Read the Full Article

{medium}

## Code Examples in This Repo

| Example | Domain | What It Shows |
|---------|--------|--------------|
| [`adapter/storage-adapter/`](../../code-samples/adapter/storage-adapter/) | Cloud storage | S3 (`putObject`) and Azure Blob (`uploadBlob`) adapted to one `ObjectStorage` interface |
| [`adapter/payment-adapter/`](../../code-samples/adapter/payment-adapter/) | Payments | Stripe (cents, PaymentIntent) and PayPal (order/capture) adapted to one `PaymentGateway` |

### How to Run

```bash
cd code-samples/adapter/storage-adapter
javac *.java && java Main

cd code-samples/adapter/payment-adapter
javac *.java && java Main
```
