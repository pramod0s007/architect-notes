# Architect's Notes

> **A Practical Decision Framework for Modern Software Design**

Pressure-first architectural thinking — design patterns, design principles, and runnable examples.

---

## Core Philosophy

Architects do not start with patterns. Architects start with pressures.

```
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

Patterns are not the destination. They are the result of good architectural reasoning.

---

## How to Use This Repository

**For theory, war stories, and architectural reasoning** → read the white papers on Medium (linked in each paper's README).

**For hands-on code examples** → explore the examples inside each paper folder. Every example compiles and runs.

---

## Series — 27 Papers, 56 Runnable Examples

### [Volume 1 — Thinking Like an Architect](volume-1-thinking-like-an-architect/)
Foundation papers. Read these before anything else.

| Paper | Title |
|-------|-------|
| [01](volume-1-thinking-like-an-architect/paper-01-why-memorizing-design-patterns-is-holding-you-back/) | Why Memorizing Design Patterns Is Holding You Back |
| [02](volume-1-thinking-like-an-architect/paper-02-the-four-architectural-buckets/) | The Four Architectural Buckets |
| [03](volume-1-thinking-like-an-architect/paper-03-the-death-of-if-else/) | The Death of if-else |

### [Volume 2 — Behavioral Design](volume-2-behavioral-design/)
The most commonly needed patterns in production systems.

| Paper | Title | Examples |
|-------|-------|---------|
| [04](volume-2-behavioral-design/paper-04-strategy-pattern-through-real-refactoring/) | Strategy Pattern Through Real Refactoring | encryption, payment gateway, pricing engine |
| [05](volume-2-behavioral-design/paper-05-state-pattern-through-a-stopwatch/) | State Pattern Through a StopWatch | stopwatch, order processing |
| [06](volume-2-behavioral-design/paper-06-command-pattern-through-banking-systems/) | Command Pattern Through Banking Systems | banking, document editor, job scheduler |
| [07](volume-2-behavioral-design/paper-07-visitor-pattern-without-uml/) | Visitor Pattern Without UML | collision engine, document processor |
| [08](volume-2-behavioral-design/paper-08-lookup-tables-vs-polymorphism/) | Lookup Tables vs Polymorphism | collision engine (table approach) |

### [Volume 3 — Enterprise Patterns](volume-3-enterprise-patterns/)
Patterns for business rules and multi-step pipelines.

| Paper | Title | Examples |
|-------|-------|---------|
| [09](volume-3-enterprise-patterns/paper-09-specification-pattern/) | Specification Pattern | product search, loan eligibility |
| [10](volume-3-enterprise-patterns/paper-10-chain-of-responsibility/) | Chain of Responsibility | request pipeline, API security pipeline |

### [Volume 4 — Creational Patterns + Architect-Level Thinking](volume-4-architect-level-thinking/)
How objects are built, and meta-level pattern judgment.

| Paper | Title | Examples |
|-------|-------|---------|
| [11](volume-4-architect-level-thinking/paper-11-builder-pattern/) | Builder Pattern | HTTP request, database config, search request |
| [12](volume-4-architect-level-thinking/paper-12-factory-pattern/) | Factory Pattern | notification factory, storage factory |
| [13](volume-4-architect-level-thinking/paper-13-when-patterns-become-anti-patterns/) | When Patterns Become Anti-Patterns | — |
| [14](volume-4-architect-level-thinking/paper-14-pattern-selection-decision-tree/) | Pattern Selection Decision Tree | — |
| [15](volume-4-architect-level-thinking/paper-15-which-patterns-still-matter-in-2026/) | [Which Patterns Still Matter in 2026?](https://medium.com/@replytopramods.aws/which-patterns-still-matter-in-2026-89d3bc154797) | — |

### [Volume 5 — Structural Patterns](volume-5-structural-patterns/)
Observer, Decorator, Proxy, Adapter — the structural foundation of modern systems.

| Paper | Title | Examples |
|-------|-------|---------|
| [16](volume-5-structural-patterns/paper-16-observer-pattern/) | [Observer Pattern](https://medium.com/@replytopramods.aws/observer-pattern-the-foundation-of-event-driven-systems-fd6c38153b34) | order events, stock price monitor |
| [17](volume-5-structural-patterns/paper-17-decorator-pattern/) | [Decorator Pattern](https://medium.com/@replytopramods.aws/decorator-pattern-adding-behavior-without-subclassing-b12b3550ecdf) | message sender, HTTP client |
| [18](volume-5-structural-patterns/paper-18-proxy-pattern/) | [Proxy Pattern](https://medium.com/@replytopramods.aws/proxy-pattern-controlling-access-to-objects-ad4888b6b592) | caching repository, lazy loading |
| [19](volume-5-structural-patterns/paper-19-adapter-pattern/) | [Adapter Pattern](https://medium.com/@replytopramods.aws/adapter-pattern-making-incompatible-interfaces-work-together-300d6a722714) | storage adapter, payment adapter |

### [Volume 6 — Clean Code & SOLID Principles](volume-6-clean-code-principles/)
The principles that make code changeable, testable, and maintainable under real organizational pressure.

| Paper | Title | Examples |
|-------|-------|---------|
| [20](volume-6-clean-code-principles/paper-20-single-responsibility-principle/) | [Single Responsibility Principle](https://medium.com/@replytopramods.aws/single-responsibility-principle-24c1b8550a4f) | order processor, user account, report generator |
| [21](volume-6-clean-code-principles/paper-21-open-closed-principle/) | [Open/Closed Principle](https://medium.com/@replytopramods.aws/open-closed-principle-7d7886947790) | fee calculator, payment rules |
| [22](volume-6-clean-code-principles/paper-22-liskov-substitution-principle/) | Liskov Substitution Principle | document hierarchy, shape composition |
| [23](volume-6-clean-code-principles/paper-23-interface-segregation-principle/) | Interface Segregation Principle | notification channels, media player |
| [24](volume-6-clean-code-principles/paper-24-dependency-inversion-principle/) | Dependency Inversion Principle | order service, notification service, report service |
| [25](volume-6-clean-code-principles/paper-25-dry-dont-repeat-yourself/) | DRY — Don't Repeat Yourself | discount calculator, address validator |
| [26](volume-6-clean-code-principles/paper-26-kiss-keep-it-simple/) | KISS — Keep It Simple | product search, config loader, HTTP handler |
| [27](volume-6-clean-code-principles/paper-27-yagni-you-arent-gonna-need-it/) | YAGNI — You Aren't Gonna Need It | discount evolution, event publisher, user repository |

---

## Repository Structure

```
architect-notes/
├── volume-1-thinking-like-an-architect/
│   ├── README.md                          ← volume overview
│   └── paper-01-.../
│       ├── README.md                      ← paper explanation + Medium link
│       ├── EXAMPLES.md                    ← classification examples
│       ├── INTERVIEWS.md                  ← interview prep questions
│       ├── NOTES.md                       ← author learning notes
│       └── example-folder-name/
│           ├── *.java                     ← runnable code
│           └── README.md                  ← class diagram, sequence flow, how to run
```

---

## Running Any Example

```bash
cd volume-X-.../paper-XX-.../example-name
javac *.java && java Main
```

---

## Author

**Pramod Srivastava** — Staff Engineer @ Adobe

Distributed systems, software architecture, AI platforms, engineering trade-offs.
