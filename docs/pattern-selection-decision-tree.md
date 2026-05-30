# Pattern Selection Decision Tree

Start with the question: **what is changing?**

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

## How to use this tree

1. Identify the primary pressure (data shape, object graph, behavior, or rules).
2. Prefer the smallest abstraction that addresses that pressure.
3. Reach for a named pattern only when the pressure is recurring and the trade-offs are understood.

## Mapped papers

| Pressure | Patterns / approaches | Paper |
|----------|----------------------|-------|
| Behavior variation | Strategy | [04](../volume-2-behavioral-design/paper-04-strategy-pattern-through-real-refactoring/) |
| State explosion | State | [05](../volume-2-behavioral-design/paper-05-state-pattern-through-a-stopwatch/) |
| Encapsulated operations | Command | [06](../volume-2-behavioral-design/paper-06-command-pattern-through-banking-systems/) |
| Interaction matrix | Visitor, Lookup | [07](../volume-2-behavioral-design/paper-07-visitor-pattern-without-uml/), [08](../volume-2-behavioral-design/paper-08-lookup-tables-vs-polymorphism/) |
| Rules variation | Specification | [09](../volume-3-enterprise-patterns/paper-09-specification-pattern/) |
| Sequential flow | Chain of Responsibility | [10](../volume-3-enterprise-patterns/paper-10-chain-of-responsibility/) |
| Complex construction | Builder | [11](../volume-4-architect-level-thinking/paper-11-builder-pattern/) |
| Creation variation | Factory | [12](../volume-4-architect-level-thinking/paper-12-factory-pattern/) |

Expanded treatment: [Paper 14 — Pattern Selection Decision Tree](../volume-4-architect-level-thinking/paper-14-pattern-selection-decision-tree/).
