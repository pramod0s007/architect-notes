# DRY — Don't Repeat Yourself

**Principle:** DRY (Don't Repeat Yourself)

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

When a discount formula lives in seven places, a one-line bug fix requires seven coordinated edits — and misses one. DRY states that every piece of knowledge should have a single, authoritative representation in the system. The pressure is not typing the same code twice; it is the divergence that happens when copies are updated inconsistently over time.

## The Pressure: One Bug Fix Requires Seven Updates and Misses One

Seven copies of the same discount logic look harmless on day one. On day 90, when the business changes the formula, six copies get updated and one does not. That one copy silently produces wrong discounts for one customer segment. DRY eliminates divergence by making the formula exist in exactly one place.

## The Principle

**Every piece of knowledge should have one authoritative home.** DRY is about knowledge, not keystrokes. Two pieces of code that look identical but represent different business concepts should stay separate. Two pieces of code that encode the same business rule must be unified.

## Pros

- A rule change requires one edit in one place — not a grep-and-pray across the codebase
- Tests for the rule are concentrated — comprehensive coverage in one test class
- Onboarding engineers find the canonical logic immediately
- Divergence bugs — the hardest category to debug — are structurally prevented

## Cons

- Wrong abstraction is worse than duplication — premature DRY couples unrelated things
- Shared code creates shared deployment risk — one change can affect many callers
- "DRY" applied to coincidental similarity produces fragile, misleading abstractions

## When NOT to Use

- When two pieces of similar code represent different business concepts that will evolve independently
- Test setup code — some duplication in tests improves readability
- When the shared abstraction requires more parameters than it saves lines

## Code Examples in This Repo

| Example | Domain | What It Shows |
|---------|--------|--------------|
| [`evolution/`](./evolution/) | Discounts | Discount formula scattered in 7 places → `DiscountCalculator` as single source of truth |
| [`address-validator/`](./address-validator/) | E-commerce | Address validation shared across registration, checkout, and shipping modules |
| [`pagination/`](./pagination/) | Data access | `PageRequest`/`PageResponse` shared across all repositories instead of per-repo copies |

### How to Run

```bash
cd volume-6-clean-code-principles/paper-25-dry-dont-repeat-yourself/evolution
javac *.java && java Main

cd volume-6-clean-code-principles/paper-25-dry-dont-repeat-yourself/address-validator
javac *.java && java Main
```
