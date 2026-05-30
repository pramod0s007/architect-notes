# Architect's Notes

> A practical decision framework for software architecture, design patterns, distributed systems, and engineering trade-offs.



## Why This Repository Exists

Most software engineering resources teach design patterns, frameworks, and technologies in isolation.

This repository takes a different approach.

Instead of starting with solutions, we start with problems.

Every abstraction, design pattern, architectural style, and engineering decision exists because it solves a recurring problem.

The goal of this repository is to help engineers understand:

* What problem exists?
* Why does the problem occur?
* What architectural pressure is being created?
* Which abstraction addresses that pressure?
* What trade-offs are introduced?

The focus is not on memorizing patterns.

The focus is on learning how architects think.



## Core Philosophy

Architects do not start with patterns.

Architects start with pressures.

```text
Code Smell
    ↓
Design Pressure
    ↓
Refactoring
    ↓
Abstraction
    ↓
Pattern
```

Patterns are not the destination.

They are the result of good architectural reasoning.



## Architect's Notes Series

### Volume 1 — Thinking Like an Architect

* Paper 01 — Why Memorizing Design Patterns Is Holding You Back
* Paper 02 — The Four Architectural Buckets
* Paper 03 — The Death of if-else

### Volume 2 — Behavioral Design

* Paper 04 — Strategy Pattern Through Real Refactoring
* Paper 05 — State Pattern Through a StopWatch
* Paper 06 — Command Pattern Through Banking Systems
* Paper 07 — Visitor Pattern Without UML
* Paper 08 — Lookup Tables vs Polymorphism

### Volume 3 — Enterprise Patterns

* Paper 09 — Specification Pattern
* Paper 10 — Chain of Responsibility
* Paper 11 — Builder Pattern
* Paper 12 — Factory Pattern

### Volume 4 — Architect-Level Thinking

* Paper 13 — When Patterns Become Anti-Patterns
* Paper 14 — Pattern Selection Decision Tree
* Paper 15 — Which Patterns Still Matter in 2026?



## Repository Structure

```text
architect-notes
│
├── volume-1-thinking-like-an-architect
├── volume-2-behavioral-design
├── volume-3-enterprise-patterns
├── volume-4-architect-level-thinking
├── diagrams
├── code-samples
└── references
```



## Design Philosophy

Every paper follows the same framework:

```text
Problem
    ↓
Code Smell
    ↓
Architectural Pressure
    ↓
Possible Solutions
    ↓
Selected Abstraction
    ↓
Trade-offs
```

The objective is not to teach design patterns as isolated concepts.

The objective is to understand when and why they emerge.



## Intended Audience

This repository is for:

* Software Engineers
* Senior Engineers
* Staff Engineers
* Architects
* Engineering Leaders
* Engineers preparing for Staff / Principal / Architect roles



## Author

**Pramod Srivastava**

Staff Backend Engineer @ Adobe

Interested in distributed systems, software architecture, AI platforms, engineering trade-offs, and scalable backend systems.
