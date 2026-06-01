# Paper 04 — Strategy Pattern: Evolution Examples

Domain: **Sorting Algorithms** — a domain where the behavior variation is unambiguous. Different algorithms produce the same result but compute it completely differently.

## The Trigger

> "Data scientists want to swap algorithms at runtime by dataset size — use the fastest algorithm for the input characteristics without redeploying."

This single sentence contains three Strategy Pattern signals:
1. "swap algorithms" — the algorithm is a variable, not a fixed method
2. "at runtime" — the variation point must be runtime-injectable
3. "by dataset size" — a meta-strategy (SmartSort) is now desirable

---

## Files

| File | Stage | Description |
|------|-------|-------------|
| `v1_NoPattern.java` | Month 1 | 2 algorithms, if-else, correct as-is |
| `v2_PressureBuilds.java` | Month 1–9 | 7 algorithms accumulated, pain points marked with `[!]` |
| `v3_StrategyApplied.java` | Refactored | Strategy interface + 7 impls + SmartSort + SortService with no if-else |

---

## Progression

**v1 (Month 1)** — Two algorithms, dead simple. No pressure. The if-else is 2 branches and both branches have been stable for months. Correct as-is.

**v2 (Month 9)** — Seven algorithms. Every addition required opening `SortService`. Pain points marked `[!]` in code:
- Each new algorithm modifies `SortService`, risking regressions in existing ones
- Algorithms cannot be unit-tested without instantiating the whole service
- The data scientists' runtime-swap requirement cannot be satisfied cleanly

**v3 (Refactored)** — `SortStrategy` interface with 7 implementations plus a `SmartSort` composite strategy that auto-selects based on dataset size. `SortService` has zero if-else.

---

## Why Strategy (not a Map or DI)

| Situation | Solution |
|---|---|
| Same algorithm, different output vocabulary (json/csv) | Map registry — Bucket 1 |
| Same workflow, different external resource (mysql/mongo) | Interface + DI — Bucket 2 |
| Different computation per variant (bubble/radix/tim) | Strategy Pattern — Bucket 3 |

Sorting algorithms are Bucket 3. Each algorithm is a fundamentally different computation. The comparison is not just about time complexity — the memory access patterns, stability characteristics, and applicability to different data shapes all differ.

---

## How to Run

```bash
javac v1_NoPattern.java
javac v2_PressureBuilds.java
javac v3_StrategyApplied.java

java v1_NoPattern
java v2_PressureBuilds
java v3_StrategyApplied
```

No external dependencies. All files are self-contained.

---

## Adding a New Algorithm (v3 only)

To add `ShellSort` in v3:
1. Create `class ShellSort implements SortStrategy { ... }`
2. Call `service.setStrategy(new ShellSort())`

Zero changes to `SortService`, zero changes to any existing strategy class. The demo in `v3_StrategyApplied.java` shows this as an anonymous class addition.
