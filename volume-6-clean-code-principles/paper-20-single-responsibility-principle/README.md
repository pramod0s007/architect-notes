# Single Responsibility Principle — One Reason to Change

**Principle:** Single Responsibility Principle (SRP)

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

When five teams own one class, every unrelated feature change forces a coordinated deployment. SRP states that a class should have one — and only one — reason to change, meaning one team, one domain concern, one axis of evolution. The pressure is not complexity; it is coupling between unrelated responsibilities that makes the simplest change risky.

## The Pressure: Coordinated Deployments for Unrelated Changes

An `OrderProcessor` that handles payment, inventory, shipping, notifications, and audit logging means a billing fix requires QA from the shipping team. SRP breaks that coupling. Five concerns become five classes. Each team deploys independently.

## The Principle

**A class should have one reason to change.** "Reason to change" means the team or business concern that owns the behavior — not just the number of methods.

## Pros

- Teams deploy independently — no coordination for unrelated changes
- Classes stay small, readable, and testable in isolation
- A change to notification logic cannot break payment logic
- Easier to onboard engineers — each class has a clear owner

## Cons

- More classes to navigate — discoverability requires good package structure
- Risk of over-splitting: separating things that always change together
- Thin classes can produce too many constructor parameters downstream

## When NOT to Use

- Simple scripts or one-off utilities where the "team" is one person
- Classes that genuinely have one job and one collaborator — don't split for the sake of splitting
- When splitting forces more shared state than the original class had

## Code Examples in This Repo

| Example | Domain | What It Shows |
|---------|--------|--------------|
| [`order-processor/`](./order-processor/) | E-commerce | Order flow split into 5 focused classes: payment, inventory, shipping, notification, audit |
| [`user-account/`](./user-account/) | Banking | Auth, profile, and notification responsibilities separated |
| [`report-generator/`](./report-generator/) | Analytics | Data fetch, format, and deliver split into distinct classes |
| [`evolution/`](./evolution/) | E-commerce | `OrderProcessor` v1 (1 class) → v2 (5 teams, 1 class) → v3 (SRP applied) |

### How to Run

```bash
cd volume-6-clean-code-principles/paper-20-single-responsibility-principle/order-processor
javac *.java && java Main

cd volume-6-clean-code-principles/paper-20-single-responsibility-principle/evolution
javac *.java && java Main
```
