# Open/Closed Principle — Open for Extension, Closed for Modification

**Principle:** Open/Closed Principle (OCP)

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

Every time a new payment method requires modifying the fee calculator, engineers must re-test all existing payment paths. OCP states that classes should be open for extension but closed for modification — new behavior arrives as new code, not as edits to working code. The pressure is the regression risk that compounds as the number of variants grows.

## The Pressure: Adding New Variants Requires Modifying Working Code

A fee calculator with a switch statement over payment types means adding crypto forces you to reopen, modify, and retest code that handles Visa, PayPal, and bank transfers — code that was already working. OCP eliminates that reopen risk.

## The Principle

**New behavior = new class. Existing classes stay closed.** Use a common interface and a registry. Adding a new variant means writing one new class and registering it — nothing else changes.

## Pros

- Existing logic is never reopened — regression risk is zero for unchanged paths
- Each payment type is independently testable
- The registry pattern makes the system self-documenting: all variants visible in one place
- Onboarding a new payment method is a one-class task

## Cons

- More classes per feature — the upfront cost is real
- Interface design must be stable; a poorly designed extension point causes more pain than a switch statement
- Over-applying OCP to genuinely stable code adds structure with no payoff

## When NOT to Use

- When you have fewer than 3 variants and no roadmap for more
- When the "extension" always requires changing the interface anyway
- Configuration-driven variation (feature flags, A/B tests) — OCP is the wrong tool

## Code Examples in This Repo

| Example | Domain | What It Shows |
|---------|--------|--------------|
| [`evolution/`](./evolution/) | Payments | Fee calculator: switch statement → 7 payment types as separate classes with Map-based registry |
| [`tax-calculator/`](./tax-calculator/) | Tax | 4 country rules as separate classes; adding a 5th = new class only |
| [`notification-formatter/`](./notification-formatter/) | Notifications | 3 channel formatters (email, SMS, push); new channel = new class |

### How to Run

```bash
cd volume-6-clean-code-principles/paper-21-open-closed-principle/evolution
javac *.java && java Main

cd volume-6-clean-code-principles/paper-21-open-closed-principle/tax-calculator
javac *.java && java Main
```
