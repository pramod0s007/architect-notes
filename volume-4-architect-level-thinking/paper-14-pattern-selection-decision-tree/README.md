# Pattern Selection Decision Tree

**Canonical reference:** [`docs/pattern-selection-decision-tree.md`](../../docs/pattern-selection-decision-tree.md)

## The Wrong Question

One of the most common questions in software design is:

> Which design pattern should I use?

Unfortunately, this question is usually asked too early.

Patterns are solutions.

Good architecture starts by understanding the problem.

The better question is:

> What kind of variation am I dealing with?

## Step 1 – Identify What Is Changing

Every design problem begins with change.

The first task is to identify the source of that change.

```text
What is changing?

├── Data
├── Object
├── Behavior
└── Rules
```

This classification immediately removes many incorrect solutions.

### Data Variation

**Examples:**

- CSV vs JSON vs XML
- Report formats
- Configuration values

**Questions:**

- Is the algorithm stable?
- Is only the data changing?

**Typical Solutions:**

- Parameters
- Templates
- Configuration
- Generics

Patterns are often unnecessary.

### Object Variation

**Examples:**

- MySQL vs MongoDB
- S3 vs Azure Blob Storage
- Email vs SMS

**Questions:**

- Is the workflow stable?
- Is the participating object changing?

**Typical Solutions:**

- Composition
- Dependency Injection
- Factory Pattern

### Behavior Variation

**Examples:**

- Encryption algorithms
- Pricing strategies
- Validation algorithms

**Questions:**

- Is the caller stable?
- Is the algorithm changing?

**Typical Solutions:**

- Strategy Pattern
- Command Pattern
- State Pattern

This is where many GoF patterns emerge.

### Rules Variation

**Examples:**

- Eligibility checks
- Search filters
- Discount engines

**Questions:**

- Are business rules growing independently?
- Are conditions multiplying?

**Typical Solutions:**

- Specification Pattern
- Rule Engines
- Decision Tables

## Step 2 – Measure The Pressure

Not every variation requires a pattern.

Ask:

- How frequently does it change?
- How expensive is modification?
- How likely is future growth?
- Is complexity increasing?

Without pressure, abstractions are usually unnecessary.

See [Paper 13 — When Patterns Become Anti-Patterns](../paper-13-when-patterns-become-anti-patterns/).

## Step 3 – Introduce The Smallest Possible Abstraction

A common mistake is jumping directly to a sophisticated pattern.

Prefer:

```text
Simple Code
      ↓
Pressure
      ↓
Small Refactoring
      ↓
Pattern
```

Not:

```text
Simple Code
      ↓
Pattern
      ↓
More Pattern
      ↓
Even More Pattern
```

## The Architect's Shortcut

When reviewing unfamiliar code:

```text
Data Changing?
      ↓
Configuration

Object Changing?
      ↓
Composition

Behavior Changing?
      ↓
Strategy / Command / State

Rules Changing?
      ↓
Specification
```

This shortcut solves a surprising number of design problems.

## Final Thoughts

Architects do not begin with patterns.

They begin with forces.

Patterns are visible.

Forces are hidden.

The ability to recognize those forces is what transforms design pattern knowledge into architectural judgment.

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
- Runnable samples for behavior and rules live under `code-samples/` (Papers 04–10).
