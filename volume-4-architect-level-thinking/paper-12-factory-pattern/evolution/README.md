# Paper 12 — Factory Pattern: Evolution Examples

**Domain:** Logger Factory

## Progression

| Version | File | Pattern | Core Problem |
|---------|------|---------|--------------|
| v1 | `v1_ScatteredNew.java` | No factory — each service does `new` | One of four services missed during production migration; logs silently dropped |
| v2 | `v2_FactoryApplied.java` | Factory Method | One place controls logger creation; migration = one file change |
| v3 | `v3_RegistryPattern.java` | Registry Pattern | Open/Closed — add new logger type with one `register()` call; zero service changes |

## Why This Domain

Logger creation is a deceptively simple example: every service logs, every environment
may log differently, and the creation detail (Console vs File vs Cloud) is infrastructure.
Services should not own that decision.

## How to Run

```bash
cd volume-4-architect-level-thinking/paper-12-factory-pattern/evolution

javac -d out v1_ScatteredNew.java v2_FactoryApplied.java v3_RegistryPattern.java

java -cp out evolution.v1_ScatteredNew
java -cp out evolution.v2_FactoryApplied
java -cp out evolution.v3_RegistryPattern
```

## Key Insight

v1's bug is realistic: one file is always missed in a migration.
v2 eliminates the bug at the cost of a switch statement in the factory.
v3 eliminates the switch statement — the factory is an open registry.

## Factory vs Registry

| | Factory (v2) | Registry (v3) |
|--|--|--|
| New logger type | Edit factory switch | One `register()` call |
| Violates Open/Closed? | Yes (switch grows) | No |
| Runtime extensibility | No | Yes |
| Complexity | Low | Low-Medium |

Use the Registry when new types arrive regularly (each team adds their logger).
Use the simple Factory when the set of types is stable and small.
