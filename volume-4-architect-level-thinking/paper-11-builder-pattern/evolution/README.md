# Paper 11 — Builder Pattern: Evolution Examples

**Domain:** Alert Configuration (monitoring alerts)

## Progression

| Version | File | Pattern | Core Problem |
|---------|------|---------|--------------|
| v1 | `v1_ConstructorProblem.java` | 8-parameter constructor | Positional errors compile silently; priority and windowSeconds swapped at 2 AM |
| v2 | `v2_SetterApproach.java` | Setter-based mutable object | Half-configured alerts register successfully; objects mutate after storage; no build-time validation |
| v3 | `v3_BuilderApplied.java` | Builder Pattern | Immutable result; named steps; `build()` validates all invariants before an object can exist |

## Why This Domain

Alert configuration is a real architect decision point: 8 fields, several optional,
with invariants that must hold (threshold > 0, recipients not empty, channel not null).
A misconfigured alert silently fails — no exception, no log, no alert.
The Builder Pattern prevents this category of bug entirely.

## How to Run

Each file has a `main()` method. Compile from the `evolution/` directory:

```bash
cd volume-4-architect-level-thinking/paper-11-builder-pattern/evolution

# Compile all three
javac -d out v1_ConstructorProblem.java v2_SetterApproach.java v3_BuilderApplied.java

# Run each
java -cp out evolution.v1_ConstructorProblem
java -cp out evolution.v2_SetterApproach
java -cp out evolution.v3_BuilderApplied
```

## Key Insight

v1 shows that the compiler cannot protect you from argument-position bugs —
`priority=300, windowSeconds=5` compiles cleanly and fires alerts for 5-second windows.

v2 fixes the naming problem but introduces mutation: the object stored in the registry
is the same reference that the creator can still modify.

v3's Builder is the only one where an invalid `AlertConfig` literally cannot be
constructed — `build()` throws before returning.

## What the Builder Buys You

1. Named steps — `.threshold(80.0)` not "position 2 of 8"
2. Immutability — no mutation after creation
3. Validation at build time — fail early, fail loudly
4. Defaults documented in Builder — not scattered across callers
5. Partial configs don't exist as valid objects
