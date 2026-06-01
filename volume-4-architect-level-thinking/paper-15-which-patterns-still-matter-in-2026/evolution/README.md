# Paper 15 — Which Patterns Still Matter in 2026: Modern Java Examples

**AI tools write these faster. Your job is to know WHEN to apply them.**

## Three Sections

| Section | Classic Form | Modern Form | Decision Rule |
|---------|-------------|-------------|---------------|
| Lambda as Strategy | `PricingStrategy` interface + class per algorithm | `Function<Product, Double>` | Use lambda when: single expression, no state, no injected dependencies |
| Record + Builder | Builder for all immutable objects | Java `record` for simple value objects | Use record when: all required, 2-4 fields, no inheritance |
| Switch Expression | `Map<Status, String>` lookup | Java 14+ switch expression | Use switch when: compile-time fixed enum, exhaustiveness needed |

## How to Run

Requires Java 16+ (for records and switch expressions).

```bash
cd volume-4-architect-level-thinking/paper-15-which-patterns-still-matter-in-2026/evolution

javac -d out ModernPatternExamples.java

java -cp out evolution.ModernPatternExamples
```

## Decision Tables

### Lambda vs Interface (Strategy)

| Condition | Use Lambda | Use Interface |
|-----------|-----------|---------------|
| Single expression | Yes | |
| No state between calls | Yes | |
| No injected dependencies | Yes | |
| Needs its own unit tests | | Yes |
| Has injected collaborators | | Yes |
| Multiple related methods | | Yes |
| Name communicates intent | | Yes |

### Record vs Builder

| Condition | Use Record | Use Builder |
|-----------|-----------|-------------|
| All fields required | Yes | |
| 2-4 fields | Yes | |
| No inheritance | Yes | |
| Has optional fields | | Yes |
| 5+ fields | | Yes |
| Same-type fields (positional confusion) | | Yes |
| Multi-field validation | | Yes |

### Switch Expression vs Map

| Condition | Use Switch | Use Map |
|-----------|-----------|---------|
| Values known at compile time (enum) | Yes | |
| Compiler exhaustiveness check needed | Yes | |
| Values loaded from config/database | | Yes |
| Set grows without recompiling | | Yes |

## What Has Not Changed

The patterns that still matter most in 2026:
- **Observer** — decoupling event producers from consumers
- **Strategy** — when algorithms are complex enough to warrant their own class
- **Proxy** — access control, caching, retry as separate layers
- **Adapter** — integrating external APIs without coupling
- **Builder** — complex object construction with optional fields and invariants

What changed is the syntax, not the intent.
`Function<T,R>` is Strategy without the ceremony.
`record` is Builder without the boilerplate.
Switch expression is a lookup table without the Map allocation.
