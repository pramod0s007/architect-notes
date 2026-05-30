# Which Patterns Still Matter in 2026?

> An opinionated architect view — not a pattern catalog.

**Core message:** Patterns survive because the **underlying pressures** survive.

Frameworks change. Languages add records, sealed types, and better DI. AI assistants write boilerplate faster. None of that removes the forces that created Strategy, Specification, or Builder in the first place.

## 1. Why Some Patterns Survive

Gang of Four patterns are not immortal because they appear in textbooks.

They survive when teams still feel the same pressures:

- Behavior that changes faster than callers
- State that explodes across transitions
- Operations that must be encapsulated, logged, or undone
- Rules that compose faster than `if` trees
- Objects that are painful to construct safely

When the pressure disappears, the pattern should disappear with it ([Paper 13](../paper-13-when-patterns-become-anti-patterns/)).

When the pressure remains, the pattern name is optional — the **force** is not.

## 2. Patterns That Still Matter

These five show up repeatedly in production systems in 2026. Each maps to a paper and, where applicable, a runnable sample in this repository.

### Strategy

**Pressure:** behavior variation without rewriting callers.

Still the default answer when algorithms swap (pricing, routing, encryption, validation). Prefer a function or enum until the second real algorithm hurts.

- Paper: [04](../../volume-2-behavioral-design/paper-04-strategy-pattern-through-real-refactoring/)
- Sample: `code-samples/strategy/encryption-example`

### State

**Pressure:** state explosion — transitions depend on current mode.

Workflows, devices, sessions, and stopwatches still outgrow boolean flags.

- Paper: [05](../../volume-2-behavioral-design/paper-05-state-pattern-through-a-stopwatch/)
- Sample: `code-samples/state/stopwatch-example`

### Command

**Pressure:** encapsulate operations — undo, queue, audit, replay.

Job systems, banking ledgers, and CQRS-style handlers still package intent as objects.

- Paper: [06](../../volume-2-behavioral-design/paper-06-command-pattern-through-banking-systems/)
- Sample: `code-samples/command/banking-example`

### Specification

**Pressure:** rules variation — composable predicates over entities.

Search, eligibility, and policy engines still beat nested conditionals.

- Paper: [09](../../volume-3-enterprise-patterns/paper-09-specification-pattern/)
- Sample: `code-samples/specification/product-search`

### Builder

**Pressure:** complex object construction — many optional fields and invariants at `build()`.

HTTP clients, database config, and search DSLs still need readable assembly.

- Paper: [11](../paper-11-builder-pattern/)
- Sample: `code-samples/builder/http-request-builder`

## 3. Situational Patterns

Use when the pressure is **measurable** — not because the name sounds professional.

### Visitor

Worth it when you have a **stable set of types** and **many operations** across them. For small matrices, a lookup table wins ([Paper 08](../../volume-2-behavioral-design/paper-08-lookup-tables-vs-polymorphism/), `code-samples/lookup/collision-engine`).

- Paper: [07](../../volume-2-behavioral-design/paper-07-visitor-pattern-without-uml/)
- Sample: `code-samples/visitor/collision-engine`

### Factory

Worth it when **which object gets created** varies by profile, channel, or environment — not for a single `new`.

- Paper: [12](../paper-12-factory-pattern/)
- Sample: `code-samples/factory/notification-factory`

### Chain of Responsibility

Worth it for **ordered pipelines** — auth, validation, rate limits, gateway filters.

- Paper: [10](../../volume-3-enterprise-patterns/paper-10-chain-of-responsibility/)
- Sample: `code-samples/chain-of-responsibility/request-pipeline`

## 4. Patterns Rarely Needed

Still on interview flashcards. Rarely the first tool on a 2026 greenfield service.

### Prototype

Cloning is usually framework-owned (serialization, copy constructors, persistence layers). Hand-rolled prototype hierarchies add little.

### Abstract Factory

Families of families made sense in desktop UI toolkits. Today: DI modules, plugin SPIs, and configuration profiles replace most Abstract Factory graphs.

### Mediator

Chat-room-style mediators are often replaced by event buses, workflow engines, or orchestration — with clearer boundaries and observability.

If you reach for these, document the **pressure** that simpler tools could not satisfy.

## 5. AI Era Impact

AI coding assistants change **how fast** code is written. They do not change **what hurts** when the design is wrong.

**What AI accelerates**

- Generating Strategy implementations, Command stubs, Builder fluency
- Producing Specification composites and test doubles
- Spreading patterns you only *might* need (Paper 13 Phase 1–2 risk)

**What AI does not remove**

- Wrong-branch abstractions (Paper 14)
- Accidental complexity from pattern stacking
- Team comprehension and deletion cost

**Architect stance in 2026**

Treat AI as a **typing accelerator**, not a **judgment substitute**. Review for pressure, not pattern count. The maturity curve in Paper 13 matters more when boilerplate is free.

## 6. Architect Takeaways

1. **Pressures survive; labels are optional.** Name the force before the pattern.
2. **Tier your toolkit:** daily (Strategy, State, Command, Specification, Builder), situational (Visitor, Factory, Chain), rarely first-class (Prototype, Abstract Factory, Mediator).
3. **Use the decision tree** ([Paper 14](../paper-14-pattern-selection-decision-tree/), `docs/pattern-selection-decision-tree.md`) before adding structure.
4. **Stop early** when warning signs appear (Paper 13).
5. **Runnable samples** in this repo are for judgment, not memorization — read code after reading forces.

Patterns are not the destination of architecture.

They are scars left by solved pressures — or warnings when the pressure was never real.

## Runnable Example

See papers and samples listed in sections 2–3. Volume 4 creational samples:

- `code-samples/builder/http-request-builder`
- `code-samples/factory/notification-factory`
