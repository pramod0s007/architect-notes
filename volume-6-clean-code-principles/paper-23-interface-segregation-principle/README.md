# Interface Segregation Principle — Clients Should Not Depend on Methods They Don't Use

**Principle:** Interface Segregation Principle (ISP)

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

When a fat interface forces every implementor to write methods they cannot support, the codebase fills with `throw new UnsupportedOperationException()` — a sign that the interface is too wide. ISP states that no client should be forced to depend on methods it does not use. The pressure is implementors writing fake or throwing methods to satisfy a contract that was designed for a different use case.

## The Pressure: Fat Interfaces Force Implementors to Lie

An `NotificationSender` interface with analytics tracking methods forces SMS providers — which have no analytics capability — to implement those methods as no-ops or throws. That lie is invisible until runtime. ISP prevents it by narrowing the interface to what each client actually needs.

## The Principle

**Many small, focused interfaces are better than one wide interface.** If an implementor writes `throw new UnsupportedOperationException`, the interface is too fat. Split it.

## Pros

- Implementors only implement what they can actually do — no lying no-ops
- Smaller interfaces are easier to mock in tests
- Adding a method to a focused interface impacts fewer implementors
- Clients depend only on the methods they use — smaller coupling surface

## Cons

- More interfaces to navigate — requires good naming discipline
- Risk of interface explosion if every method gets its own interface
- Composing multiple interfaces in signatures can become verbose

## When NOT to Use

- When all implementors genuinely use all methods — do not split for the sake of splitting
- Framework integration points where the fat interface is externally imposed
- When the "unused methods" are optional hooks with sensible defaults

## Code Examples in This Repo

| Example | Domain | What It Shows |
|---------|--------|--------------|
| [`evolution/`](./evolution/) | Notifications | SMS forced to implement analytics methods — fat interface split into focused ones |
| [`media-player/`](./media-player/) | Media | Mobile player forced to implement recording/streaming — split by capability |
| [`worker-interface/`](./worker-interface/) | HR system | Robot worker forced to implement `eat()` and `sleep()` — `Workable` split from `Biological` |

### How to Run

```bash
cd volume-6-clean-code-principles/paper-23-interface-segregation-principle/evolution
javac *.java && java Main

cd volume-6-clean-code-principles/paper-23-interface-segregation-principle/worker-interface
javac *.java && java Main
```
