# When Patterns Become Anti-Patterns

**Pattern:** Pattern Anti-Patterns (Meta)

---

## Read the Full Article on Medium

[When Patterns Become Anti-Patterns](https://medium.com/p/309e57533027)

---
## What This Paper Is About

This may be the most important paper in the series. It covers what happens when patterns are applied without pressure — and why that outcome is worse than not knowing the patterns at all.

**A pattern without pressure is not architecture. It is ceremony.**

## The Pattern Maturity Curve

### Phase 1 — Discovery
You learn a pattern. Everything looks like a problem it can solve. Strategy Pattern everywhere. Factory Pattern everywhere. This phase is exciting and dangerous.

### Phase 2 — Overuse
You introduce abstractions before they are earned.
- Interfaces with one implementation for 18 months
- Factories that create a single object type
- Builders for objects with 3 fields
- Visitors for 4 collision pairs

The system looks sophisticated. Complexity grows faster than business value.

### Phase 3 — Architectural Judgment
You stop asking "Can I use this pattern?" and start asking "Should I use this pattern?"

This is where architectural thinking begins.

## Five Named Anti-Patterns

| Anti-Pattern | Symptom | Fix |
|-------------|---------|-----|
| Strategy Explosion | One interface, one implementation | Inline until second real algorithm appears |
| Factory Hell | `AbstractFactoryFactory` | Factory method or DI registration |
| Inheritance Abuse | `BaseService` with 40 protected hooks | Composition + Strategy for the varying step |
| Visitor Overengineering | Visitor for 4 collision pairs | `Map<Key, Action>` — 20 lines (Paper 08) |
| Premature Abstraction | `Repository` interface with one JPA impl for 2 years | Concrete class until second impl is real |

## The Three Questions

Before applying any pattern:
1. **What pressure is growing?** (Measure it. If you can't point to a signal, it may not exist.)
2. **What happens if we wait one more sprint?** (If nothing bad, wait.)
3. **What is the removal cost if we're wrong?** (Low removal cost = safer to wait.)

## The Staff Engineer Signal

Most interview rubrics test whether you know patterns. Staff-level rubrics test whether you know when NOT to use them.

Weak answer: "We switched to microservices."
Strong answer: Describe the measurable pain, the simpler replacement, and the condition that would reintroduce the pattern.

## Read the Full Article


## This Paper Has No Code Examples

The anti-pattern examples are in the other papers — specifically by showing the **before** state that does not need a pattern yet. See:
- [`strategy/encryption-example/`](../../volume-2-behavioral-design/paper-04-strategy-pattern-through-real-refactoring/encryption-example/) — shows the moment Strategy becomes justified
- [`state/order-processing/`](../../volume-2-behavioral-design/paper-05-state-pattern-through-a-stopwatch/order-processing/) — shows what State Pattern removes (the 42-branch mess)
