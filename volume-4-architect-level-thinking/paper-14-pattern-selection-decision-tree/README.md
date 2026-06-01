# Pattern Selection Decision Tree

**Pattern:** Decision Framework (Meta)

---

## Read the Full Article on Medium

_Article coming soon on Medium._

This repository focuses on runnable code examples. The white paper covers theory, war stories, and architectural reasoning in depth.

---
## What This Paper Is About

The question "which pattern should I use?" is almost always asked too early. This paper gives you a decision tree for selecting the right pattern in under 60 seconds — starting from the pressure, not the solution.

## The Decision Tree

```
Is there measurable pressure? (growth, pain, merge conflicts)
    → No: don't add a pattern yet
    → Yes: continue

What is changing?
    → Only data/config:     Parameterize. No pattern needed.
    → Object/provider:      Interface + Composition (+ Factory if creation varies)
    → Algorithm itself:
          → Operations need to travel? (undo/queue/audit) → Command
          → Behavior depends on state?                    → State
          → Otherwise                                     → Strategy
    → Business rules grow:
          → Runtime-configurable?  → Rule Engine
          → Otherwise              → Specification
```

## The 60-Second Shortcut

| What changes | Reach for |
|-------------|-----------|
| Data | Config / parameterize |
| Object | Composition + Interface |
| Algorithm | Strategy / Command / State |
| Rules | Specification |

## Common Wrong Branches

**Strategy applied to Data Variation** — report formats differ, algorithm same → parameterize, not Strategy interface.

**Specification applied to Behavior Variation** — pricing algorithms are computations, not predicates → Strategy, not Specification.

**Factory for one type** — creation doesn't vary → `new` at call site is correct.

## Read the Full Article


## The Tree in Action

Each paper in this series is one branch of this tree applied to a real domain:

| Branch | Paper | Code |
|--------|-------|------|
| Behavior variation | Paper 04 | `strategy/` examples |
| State variation | Paper 05 | `state/` examples |
| Behavior encapsulation | Paper 06 | `command/` examples |
| Rules variation | Paper 09 | `specification/` examples |
| Object variation | Paper 12 | `factory/` examples |
| Construction complexity | Paper 11 | `builder/` examples |
