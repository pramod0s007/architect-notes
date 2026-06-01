# Lookup Tables vs Polymorphism

**Pattern:** Lookup Tables (Architectural Decision)

---

## Read the Full Article on Medium

_Article coming soon on Medium._

This repository focuses on runnable code examples. The white paper covers theory, war stories, and architectural reasoning in depth.

---
## What This Paper Is About

Not every matrix of behaviors needs a class hierarchy. Sometimes a `Map<Key, Action>` is the correct architecture — simpler, faster, and more configurable than Visitor Pattern or polymorphism.

This paper covers how to make that decision.

## The Core Question

**Is the matrix finite and stable, or open and growing?**

| Dimension | Lookup Table | Polymorphism/Visitor |
|-----------|-------------|---------------------|
| Matrix size | Small (≤20 pairs) | Any |
| Type set growth | Stable | Open |
| Exhaustiveness check | Runtime | Compile time |
| New behavior | Add map entry | New class |
| New type | Add N entries | Update all visitors |
| Configuration | Possible from DB/config | Requires deployment |
| Performance | O(1) | Virtual dispatch |

## When a Lookup Table Wins

**Finite, stable matrix** — you know all combinations and they rarely change.

```java
Map<CollisionKey, CollisionAction> table = Map.of(
    key(SHIP, STATION),    (a, b) -> shipHitsStation(a, b),
    key(SHIP, COMET),      (a, b) -> shipHitsComet(a, b),
    key(STATION, ASTEROID),(a, b) -> stationHitsAsteroid(a, b)
);

table.getOrDefault(key(a, b), NO_OP).resolve(a, b);
```

**Config-driven behavior** — non-engineers need to change behavior without a deployment.

**State machine transitions** — `(currentState, event) → (nextState, action)` as a table row.

## When Polymorphism Wins

- **Type set is open** — new types arrive regularly and each needs behavior with all existing types
- **Compiler safety matters** — missing a case should be a compile error, not a runtime null
- **Multiple operations** over the same stable types — one new Visitor per operation beats one new Map per operation

## Read the Full Article


## Code Examples in This Repo

| Example | Domain | What It Shows |
|---------|--------|--------------|
| [`lookup/collision-engine/`](./collision-engine/) | Game engine | Same collision problem as Paper 07 — solved with a Map instead of Visitor |

### How to Run

```bash
cd code-samples/lookup/collision-engine
javac *.java && java Main
```

Compare the output with `visitor/collision-engine/` — identical behavior, different structure.
