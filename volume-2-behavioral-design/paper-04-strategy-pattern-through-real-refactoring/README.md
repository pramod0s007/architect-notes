# Strategy Pattern Through Real Refactoring

**Pattern:** Strategy Pattern

---

## Read the Full Article on Medium

_Article coming soon on Medium._

This repository focuses on runnable code examples. The white paper covers theory, war stories, and architectural reasoning in depth.

---
## What This Paper Is About

Strategy Pattern is one of the most well-known GoF patterns — and one of the most over-applied. Most tutorials show you the structure. This paper shows the **pressure that makes it necessary** and the **threshold condition** for introduction.

## The Pressure: Behavior Variation

Strategy Pattern addresses one pressure: when the same caller needs to trigger different algorithms, and those algorithms change at a different rate than the caller.

**The threshold condition:** introduce Strategy when the cost of modifying the conditional chain exceeds the cost of the interface abstraction. Signals:
- Merge conflicts on the same method from multiple engineers
- Every new algorithm requires modifying the same test file
- The method grows without bound

## The Refactoring Pattern

```
Before:
  Caller → if (type == X) { algorithmX() }
           if (type == Y) { algorithmY() }

After:
  Caller → strategy.execute()
  StrategyX implements Strategy
  StrategyY implements Strategy
```

## Pros

- Adding a new algorithm = one new class, zero changes to caller
- Each algorithm is independently testable
- Caller stays stable regardless of how many algorithms exist
- Runtime algorithm switching without recompilation

## Cons

- Overhead for simple cases — 2–3 algorithms with no growth signal rarely justify the interface
- Client must know which strategy to inject (or use Factory)
- Increases class count

## When NOT to Use

- Two algorithms, stable for 18 months, no growth signal → keep the if-else
- When lambdas suffice: `Function<Input, Output>` often replaces a named Strategy interface in modern Java
- When the "algorithm" is actually just data variation (different config values, not different computations)

## Read the Full Article


## Code Examples in This Repo

| Example | Domain | What It Shows |
|---------|--------|--------------|
| [`strategy/encryption-example/`](./encryption-example/) | Security | AES, DES, BLOWFISH — the classic Strategy refactoring |
| [`strategy/payment-gateway/`](./payment-gateway/) | Payments | PayPal, Stripe, Razorpay — runtime provider switching |
| [`strategy/pricing-engine/`](./pricing-engine/) | E-commerce | Premium/Employee/Partner/Standard tiers — 4 pricing algorithms |

### How to Run

```bash
cd code-samples/strategy/encryption-example
javac *.java && java Main

cd code-samples/strategy/payment-gateway
javac *.java && java Main

cd code-samples/strategy/pricing-engine
javac *.java && java Main
```
