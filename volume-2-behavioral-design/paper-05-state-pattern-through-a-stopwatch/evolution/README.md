# Paper 05 — State Pattern: Evolution Examples

Domain: **Vending Machine** — a domain with clear, relatable states that naturally evolve from 3 to 6 as product requirements grow.

## The Problem

When the number of states grows, two things happen in naive code:
1. Every action method (insertCoin, selectProduct, cancel) must add a new case for each new state
2. "What can the machine do in MAINTENANCE mode?" cannot be answered by reading one place

With 6 states and 5 actions, that is 30 case blocks to maintain. Each new state adds 5 more.

---

## Files

| File | Stage | States | Approach |
|------|-------|--------|----------|
| `v1_NoPattern.java` | Month 1 | 3 (IDLE, COIN_INSERTED, DISPENSING) | Integer constants |
| `v2_EnumApproach.java` | Month 3 | 3 states | Enum — better readability, same structural problem |
| `v3_StateExplosion.java` | Month 6 | 6 states (add: OUT_OF_STOCK, MAINTENANCE, CHANGE_DISPENSE) | Enum + switch — 24+ case blocks |
| `v4_StatePatternApplied.java` | Refactored | 6 states | State Pattern — each state is one class |

---

## Progression

**v1 (Month 1)** — Integer flags (0/1/2). 3 states, 3 actions, 9 if-else branches. Correct and minimal. No pattern needed.

**v2 (Month 3)** — Integer flags → enum. A common "improvement" that solves readability but not the structural problem. The behavior for each state is still scattered across all methods.

**v3 (Month 6)** — Three new states added. Each addition required opening every action method. The comment `[!]` marks each pain point. The cost is now 6 states × 4 methods = 24 case blocks. A 7th state requires 4 more case insertions across 4 existing methods.

**v4 (Refactored)** — `VendingMachineState` interface. Each state is a class. `VendingMachineContext` has zero switch/if-else — all calls are `state.insertCoin(this, amount)`. To add a 7th state, create one new class and update only the states that transition into it.

---

## Key Insight: State vs Strategy

| | Strategy Pattern | State Pattern |
|---|---|---|
| Encapsulates | ONE algorithm | ALL behavior for a state |
| Who decides transitions? | Context (external) | State objects (internal) |
| States know about context? | No — stateless | Yes — call ctx.setState() |
| Swap trigger | External call at any time | From within action methods |

Vending machines need State Pattern because:
- The machine transitions ITSELF (dispensing → idle, not the caller)
- Each state has different behavior across MULTIPLE actions
- The state graph is part of the domain logic, not a client concern

---

## How to Run

```bash
javac v1_NoPattern.java
javac v2_EnumApproach.java
javac v3_StateExplosion.java
javac v4_StatePatternApplied.java

java v1_NoPattern
java v2_EnumApproach
java v3_StateExplosion
java v4_StatePatternApplied
```

No external dependencies. All files are self-contained.
