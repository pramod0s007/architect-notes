# Interview Roadmap

How to use this repository across career stages — from pattern recognition to system forces.

```text
Junior
  ↓
Patterns

Senior
  ↓
Tradeoffs

Staff
  ↓
Architectural Pressure

Architect
  ↓
System Forces
```

## Junior — Patterns (with context)

**Goal:** Recognize pressures, not memorize UML.

**Read**

- Papers [01](../volume-1-thinking-like-an-architect/paper-01-why-memorizing-design-patterns-is-holding-you-back/) – [03](../volume-1-thinking-like-an-architect/paper-03-the-death-of-if-else/)
- Papers [04](../volume-2-behavioral-design/paper-04-strategy-pattern-through-real-refactoring/) – [06](../volume-2-behavioral-design/paper-06-command-pattern-through-banking-systems/)
- Run `code-samples` for 04–06

**Interview signal:** You can explain *why* Strategy differs from Factory.

## Senior — Tradeoffs

**Goal:** Choose between valid options (Visitor vs lookup, Command vs inline).

**Read**

- Papers [07](../volume-2-behavioral-design/paper-07-visitor-pattern-without-uml/) – [10](../volume-3-enterprise-patterns/paper-10-chain-of-responsibility/)
- [Pattern selection tree](pattern-selection-decision-tree.md)

**Interview signal:** You articulate deletion cost and team readability.

## Staff — Architectural Pressure

**Goal:** Name forces before solutions; spot overengineering.

**Read**

- Papers [13](../volume-4-architect-level-thinking/paper-13-when-patterns-become-anti-patterns/) – [15](../volume-4-architect-level-thinking/paper-15-which-patterns-still-matter-in-2026/)
- [Architecture map](architecture-map.md)

**Interview signal:** You describe when **not** to apply a pattern.

## Architect — System Forces

**Goal:** Connect design choices to reliability, org scale, and change frequency.

**Read**

- Paper [14](../volume-4-architect-level-thinking/paper-14-pattern-selection-decision-tree/) in full
- Paper [02](../volume-1-thinking-like-an-architect/paper-02-the-four-architectural-buckets/) as classification lens
- [Staff engineer roadmap](staff-engineer-roadmap.md)

**Interview signal:** You frame discussions in forces, constraints, and trade-offs — not pattern names.
