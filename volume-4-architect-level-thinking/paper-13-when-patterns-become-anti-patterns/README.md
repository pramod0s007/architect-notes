# When Patterns Become Anti-Patterns

## Why This Paper Exists

Patterns solve **pressure**.

When pressure is weak or misunderstood, the pattern becomes ceremony.

This may be the most valuable paper in the series.

## The Real Problem

**Premature or misapplied abstraction.**

Teams apply patterns because they recognize the name — not because the pressure is present.

## The Pattern Maturity Curve

Most engineers experience design patterns in three phases.

### Phase 1 – Discovery

The engineer learns a new pattern.

Everything suddenly looks like a problem that can be solved with that pattern.

Examples:

- Strategy everywhere
- Factory everywhere
- Builder everywhere

This phase is exciting but dangerous.

### Phase 2 – Overuse

The engineer begins introducing abstractions before they are needed.

Common symptoms:

- Interfaces with only one implementation
- Factories that create a single object
- Builders for objects with three fields
- Visitors for two object types

Complexity grows faster than business value.

### Phase 3 – Architectural Judgment

The engineer stops asking:

> "Can I use this pattern?"

and starts asking:

> "Should I use this pattern?"

At this stage patterns become tools rather than goals.

This is where architectural thinking begins.

## Five Warning Signs Of Overengineering

- More abstractions than business logic
- Interfaces with a single implementation for years
- Factories that create one object
- Builders for trivial objects
- Patterns introduced for hypothetical future requirements

When these signs appear, the system may be suffering from **accidental complexity** rather than **essential complexity**.

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
