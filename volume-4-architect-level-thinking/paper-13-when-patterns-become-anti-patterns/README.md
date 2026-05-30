# When Patterns Become Anti-Patterns

## Why This Paper Exists

Patterns solve **pressure**.

When pressure is weak or misunderstood, the pattern becomes ceremony.

This may be the most valuable paper in the series.

## The Real Problem

**Premature or misapplied abstraction.**

Teams apply patterns because they recognize the name — not because the pressure is present.

## Failure Modes

### Strategy Explosion

Every tiny variation gets an interface.

```java
interface GreetingStrategy { String greet(); }
class MorningGreeting implements GreetingStrategy { ... }
class AfternoonGreeting implements GreetingStrategy { ... }
```

Two branches did not need a strategy hierarchy.

### Factory Hell

Factories that create factories.

Callers still know every type — but now through three layers of indirection.

### Inheritance Abuse

Deep hierarchies for one behavior difference.

Composition and Strategy often replace fragile base classes.

### Visitor Overengineering

Visitor applied to a 3×3 matrix that a lookup table would solve in twenty lines.

### Premature Abstraction

Interfaces before the second implementation exists.

"No spec, no abstraction" is often the right default.

## Architect Rule

Ask before applying any pattern:

1. What pressure is growing?
2. What happens if we wait one more sprint?
3. What is the removal cost if we are wrong?

## Design Pressure

```text
Misidentified Pressure
        ↓
Pattern as Default
        ↓
Anti-Pattern
```

## Key Takeaways

- A pattern without pressure is noise.
- Simpler structures (functions, tables, DI) often win.
- Refactor **toward** patterns when pain is measurable.
- Paper 14 and `docs/pattern-selection-decision-tree.md` help choose before over-building.
