# Coupling — Hidden Dependencies and How They Spread

**Principle:** Coupling (Content, Common, Control, Stamp, Data)

---

## Read the Full Article on Medium

[Coupling — Hidden Dependencies and How They Spread](https://medium.com/@replytopramods.aws/coupling-hidden-dependencies-and-how-they-spread-c5bdbe15ba08)

---

## What This Paper Is About

A change in one module breaks another module its own team has never seen, and neither team can predict it without reading the other's source. Coupling is not one thing — it's a spectrum, from content coupling (reaching into another module's internals) down to data coupling (sharing only the exact primitive needed). The pressure is: how much of another module's internal behavior do you have to understand correctly to call it safely?

## The Pressure: Control Coupling Hiding in a Boolean

A `boolean skipTaxRounding` parameter on `InvoiceGenerator` looks like the simplest possible option. Three teams call it with three different guesses about what `true` and `false` mean. One guess is wrong. The bug ships for four months before a one-cent invoice mismatch gets noticed.

## The Principle

**Coupling is a spectrum, not a binary — data coupling is the goal, not zero coupling.** Content and control coupling are the dangerous ones: they let a caller be wrong without any compiler error. The fix is to replace flags and exposed internals with named, self-documenting collaborators.

## Pros

- Data coupling keeps call sites traceable — "Go to Definition" still works
- Naming a policy/behavior explicitly removes the need to read the callee's source to use it correctly
- Encapsulating internal state (defensive copies) prevents an entire class of cross-team incidents

## Cons

- Over-decoupling into events/message buses trades compile-time traceability for runtime invisibility
- Interfaces added where there's no real variation are ceremony, not decoupling
- Coupling analysis takes real discipline — it's easy to declare "not coupled" without checking which type of coupling remains

## When NOT to Use

- Within a single team's own module boundary, where two classes always change together for the same reason
- When the abstraction you'd introduce can't be given a real name (see Paper 24, DIP)
- On startup-critical, debugger-stepped paths where a direct call is more valuable than an untraceable event

## Code Examples in This Repo

| Example | Domain | What It Shows |
|---------|--------|--------------|
| [`evolution/`](./evolution/) | Pricing & Billing | Content coupling (exposed mutable list) and control coupling (boolean flag) — pressure, then fix |
| [`shipping-quote/`](./shipping-quote/) | Logistics | Stamp coupling — passing a whole struct when two fields were needed |
| [`shared-config/`](./shared-config/) | Promotions | Common coupling — two services silently clobbering the same static field |

### How to Run

```bash
cd volume-7-design-principles/paper-29-coupling/evolution/v1
javac *.java && java Main

cd volume-7-design-principles/paper-29-coupling/evolution/v2
javac *.java && java Main

cd volume-7-design-principles/paper-29-coupling/evolution/v3
javac *.java && java Main

cd volume-7-design-principles/paper-29-coupling/shipping-quote
javac *.java && java Main

cd volume-7-design-principles/paper-29-coupling/shared-config
javac *.java && java Main
```
