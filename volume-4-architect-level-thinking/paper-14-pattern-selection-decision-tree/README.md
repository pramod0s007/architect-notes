# Pattern Selection Decision Tree

## Signature Content

This paper expands the repository's canonical decision tree into an architect-level guide.

**Canonical reference:** [`docs/pattern-selection-decision-tree.md`](../../docs/pattern-selection-decision-tree.md)

## Start Here

**What is changing?**

```text
What is changing?

├── Data
│      ↓
│   Templates
│   Configuration
│
├── Object
│      ↓
│   DI
│   Composition
│
├── Behavior
│      ↓
│   Strategy
│   Command
│   State
│
└── Rules
       ↓
   Specification
   Rule Engine
```

## How Architects Use This

1. Name the pressure before naming the pattern.
2. Map the pressure branch to papers 04–12 in this repository.
3. Validate with Paper 13 — is the pattern earning its complexity?
4. Confirm with Paper 15 — is the pattern still worth it in modern codebases?

## Branch Notes

### Data changing

Prefer templates and configuration over subclass forests.

### Object graph changing

Prefer DI and composition before Factory/Abstract Factory.

### Behavior changing

Strategy, Command, and State from Volume 2 — each addresses a distinct pressure.

### Rules changing

Specification and rule engines — Volume 3.

## Design Pressure

```text
Unclear problem framing
        ↓
Decision tree
        ↓
Right-sized abstraction
```

## Key Takeaways

- The tree is a **navigation tool**, not a checklist.
- Wrong branch → wrong pattern → anti-pattern (Paper 13).
- Link every production decision to a pressure, not a glossary term.
