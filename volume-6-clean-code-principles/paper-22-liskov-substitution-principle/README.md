# Liskov Substitution Principle — Subclasses Must Honor the Contract

**Principle:** Liskov Substitution Principle (LSP)

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

When a subclass throws an exception for a method the base class promises to support, callers start adding `instanceof` checks to defend themselves. LSP states that a subclass must be substitutable for its base class without breaking caller behavior. The pressure is `instanceof` proliferation — a signal that the hierarchy is lying about its contract.

## The Pressure: instanceof Checks Spreading Through Callers

A `ReadOnlyDocument` that extends `Document` but throws on `save()` forces every caller to check what kind of document it has before calling save. That check is a contract violation made visible. LSP violations do not stay local — they metastasize into every caller.

## The Principle

**If it cannot do what the base class promises, it should not extend the base class.** When a subtype cannot honor the full contract, split the interface instead of overriding with a throw.

## Pros

- Callers write against the abstraction — no defensive `instanceof` checks
- Hierarchies stay honest — every subtype can be used anywhere the base is expected
- Easier to test — any implementation can be substituted in test fixtures

## Cons

- LSP violations are often discovered late — the pressure only appears when callers try to use the subtype
- Fixing an LSP violation often requires breaking an existing hierarchy
- Interface proliferation if over-applied to minor behavioral differences

## When NOT to Use

- When the "violation" is a postcondition strengthening that all callers will accept
- Framework base classes with intentional no-op defaults — LSP applies to semantic contracts, not every override
- When the hierarchy is internal to one class and never exposed to callers

## Code Examples in This Repo

| Example | Domain | What It Shows |
|---------|--------|--------------|
| [`evolution/`](./evolution/) | Documents | `ReadOnlyDocument` throws on `save()` — LSP violation, then interface split fix |
| [`payment-processor/`](./payment-processor/) | Payments | `CryptoPayment` cannot refund — interface split into `Chargeable` and `Refundable` |
| [`shape-area/`](./shape-area/) | Geometry | `Square extends Rectangle` breaks `testArea()` — classic LSP example |

### How to Run

```bash
cd volume-6-clean-code-principles/paper-22-liskov-substitution-principle/evolution
javac *.java && java Main

cd volume-6-clean-code-principles/paper-22-liskov-substitution-principle/shape-area
javac *.java && java Main
```
