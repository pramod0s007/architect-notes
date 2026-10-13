# Abstraction — Choosing the Right Level

**Principle:** Abstraction (as a promise about hidden information, not a synonym for "interface")

---

## Read the Full Article on Medium

[Abstraction — Choosing the Right Level](https://medium.com/@replytopramods.aws/abstraction-choosing-the-right-level-a8999030ac5a)

---

## What This Paper Is About

An abstraction is a promise about what the caller does not need to know. It fails in two opposite directions: too little (the type signature can't express real outcomes, so implementations lie to fit) and too much (the interface includes capabilities only one implementation actually has, forcing the rest to stub or throw). This paper shows both failure modes with the same running example — a payment gateway abstraction — and how to find the level where every implementation can honor every method truthfully.

## The Pressure: A Boolean That Can't Say "Pending"

`boolean charge(double amount)` works fine for a payment processor that settles instantly. It has no way to express "this will take 1-3 business days," so the implementation that needed to say that returned `true` immediately instead. A refund got processed against a payment that hadn't actually cleared.

## The Principle

**An abstraction is correct only where every implementation behind it can be swapped without the caller's reasoning about correctness changing.** The moment a caller has to ask "which implementation is this?" to write correct code, the abstraction chose the wrong level.

## Pros

- The right-level abstraction lets you add a new implementation without touching any caller
- Capability-split interfaces (vs. one bloated interface) turn "does this implementation support X?" from a runtime surprise into a compile-time fact
- Forces the real design conversation (e.g. "can settlement be pending?") to happen at interface-design time, not in a postmortem

## Cons

- Too-little abstraction is often invisible until the second/third implementation arrives and exposes the gap
- Too-much abstraction (one bloated interface) is easy to justify in the moment ("just in case") and expensive to split later
- Capability-split interfaces mean more types to track — worth it only once there are genuinely different capability sets

## When NOT to Use

- One implementation, no second one in sight — extracting an interface here is a name, not a boundary (see YAGNI, Paper 27)
- When the "variation" is a configuration value (a rate, a URL), not a behavior — a parameter solves it, no interface hierarchy needed
- Don't wrap a well-abstracted library type in your own abstraction "just in case" you switch libraries later

## Code Examples in This Repo

| Example | Domain | What It Shows |
|---------|--------|--------------|
| [`evolution/`](./evolution/) | Payments | `boolean` gateway (too little) -> `SettlementStatus` enum (right level) -> bloated interface split by capability (fixing too much) |

### How to Run

```bash
cd volume-7-design-principles/paper-28-abstraction/evolution/v1
javac *.java && java Main

cd volume-7-design-principles/paper-28-abstraction/evolution/v2
javac *.java && java Main

cd volume-7-design-principles/paper-28-abstraction/evolution/v3
javac *.java && java Main
```
