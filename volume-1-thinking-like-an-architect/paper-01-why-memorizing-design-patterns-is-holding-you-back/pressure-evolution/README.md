# Pressure Evolution — Paper 01 Code Examples

This folder demonstrates the **pressure-first framework** from Paper 01 through two complete progressions.

The structure is intentional: **wrong → better → correct → pattern**. You cannot understand why a pattern exists until you feel the pressure that creates it.

---

## Read the White Paper First

[Why Memorizing Design Patterns Is Holding You Back](https://medium.com/@replytopramods.aws/why-memorizing-design-patterns-is-holding-you-back-6b6dfa8d7d7c)

---

## Example 1 — NotificationService (Behavior Variation Pressure)

| File | Stage | What It Shows |
|------|-------|--------------|
| `v1_NoPressure.java` | Month 1 | 2 channels, simple if-else. **Correct as-is. Do not refactor.** |
| `v2_PressureBuilding.java` | Month 6 | 5 channels, merge conflicts starting. Pressure signal detected. |
| `v3_PatternEmerges.java` | Month 9 | Strategy Pattern introduced — because modification cost exceeded abstraction cost. |

**Pressure type:** Behavior Variation — same caller, algorithm changes per channel.

**Key lesson:** The if-else in v1 is correct. The refactoring in v3 is justified. The difference is the growth rate between v1 and v2.

---

## Example 2 — TrafficLight (State Explosion Pressure)

| File | Stage | What It Shows |
|------|-------|--------------|
| `v4_TrafficLight_StatePressure.java` | All stages | TrafficLightV1 (flags) → V2 (constants) → V3 (enum) → V4 (State Pattern) |

**Pressure type:** State Explosion — adding a new state (FlashingRed) in V2 requires editing every method. In V4, it requires one new class.

**Key lesson:** State Pattern is not about the number of if-statements. It is about the cost of adding a new state.

---

## How to Run

```bash
# Example 1 — NotificationService evolution
cd pressure-evolution
# Copy v3 contents to a runnable file
javac v3_PatternEmerges.java && java Main

# Example 2 — TrafficLight evolution
javac v4_TrafficLight_StatePressure.java && java TrafficLightDemo
```

---

## The Pressure-First Pattern

```
v1: Simple, correct code (2 channels)
        ↓ Time passes. Requirements grow.
v2: Pressure appears (5 channels, merge conflicts)
        ↓ Modification cost > Abstraction cost
v3: Pattern emerges (Strategy — not because book said so)

v1: Integer flags, magic numbers
        ↓ Named constants (better)
        ↓ Enum (type-safe)
        ↓ States multiply, transition cost grows
v4: State Pattern emerges (each state owns its behavior)
```

**The pattern is the result. The pressure is the cause.**
