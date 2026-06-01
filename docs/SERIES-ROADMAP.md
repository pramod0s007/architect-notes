# Series Roadmap — All 23 GoF Patterns + Enterprise Patterns

> One paper per pattern. Pressure-first framework throughout.

---

## Volume 1 — Thinking Like an Architect (3 papers)

Foundation papers. Must be read before the pattern volumes.

| # | Title | Status | Diagram |
|---|-------|--------|---------|
| 01 | Why Memorizing Design Patterns Is Holding You Back | ✅ Complete | pressure-to-pattern.png |
| 02 | The Four Architectural Buckets | ✅ Complete | four-buckets.png |
| 03 | The Death of if-else | ✅ Complete | — |

---

## Volume 2 — Behavioral Patterns (6 papers)

The most commonly needed patterns. High pressure in production systems.

| # | Title | GoF Pattern | Status | Diagram |
|---|-------|------------|--------|---------|
| 04 | Strategy Pattern Through Real Refactoring | Strategy | ✅ Complete | strategy-refactoring.png |
| 05 | State Pattern Through a StopWatch | State | ✅ Complete | state-explosion.png |
| 06 | Command Pattern Through Banking Systems | Command | ✅ Complete | command-flow.png |
| 07 | Visitor Pattern Without UML | Visitor | ✅ Complete | — |
| 08 | Lookup Tables vs Polymorphism | — (architectural) | ✅ Complete | — |
| **16** | **Observer — The Foundation of Event-Driven Systems** | Observer | 🔲 Planned | — |

---

## Volume 3 — Enterprise Patterns (3 papers)

Patterns for business rules, pipelines, and complex domain logic.

| # | Title | Pattern | Status | Diagram |
|---|-------|---------|--------|---------|
| 09 | Specification Pattern | — (enterprise) | ✅ Complete | specification-composition.png |
| 10 | Chain of Responsibility | Chain of Resp. | ✅ Complete | chain-pipeline.png |
| **21** | **Template Method — Algorithms With Hooks** | Template Method | 🔲 Planned | — |

---

## Volume 4 — Creational Patterns + Architect Thinking (7 papers)

How objects are built and created. Plus meta-level architectural judgment.

| # | Title | GoF Pattern | Status | Diagram |
|---|-------|------------|--------|---------|
| 11 | Builder Pattern | Builder | ✅ Complete | — |
| 12 | Factory Pattern | Factory Method | ✅ Complete | — |
| **17** | **Abstract Factory — When Families of Objects Swap** | Abstract Factory | 🔲 Planned | — |
| **22** | **Singleton — The Pattern Everyone Gets Wrong** | Singleton | 🔲 Planned | — |
| 13 | When Patterns Become Anti-Patterns | — (meta) | ✅ Complete | maturity-curve.png |
| 14 | Pattern Selection Decision Tree | — (meta) | ✅ Complete | decision-tree.png |
| 15 | Which Patterns Still Matter in 2026 | — (meta) | ✅ Complete | pattern-tiers-2026.png |

---

## Volume 5 — Structural Patterns (7 papers)

**Entirely missing from the current series. Highest priority for Phase 4.**

| # | Title | GoF Pattern | Pressure | Priority |
|---|-------|------------|---------|---------|
| **18** | **Decorator — Wrapping Behavior Without Subclassing** | Decorator | Adding responsibilities dynamically | 🔴 Critical |
| **19** | **Proxy — Controlling Access to Objects** | Proxy | Lazy loading, caching, security, remote | 🔴 Critical |
| **20** | **Adapter — Making Incompatible Interfaces Work** | Adapter | Legacy integration, third-party APIs | 🔴 Critical |
| **23** | **Facade — Simplifying Complex Subsystems** | Facade | SDK design, service layer | 🟡 High |
| **24** | **Composite — Trees and Part-Whole Hierarchies** | Composite | File systems, UI components, org charts | 🟡 High |
| **25** | **Bridge — Separating Abstraction from Implementation** | Bridge | Platform independence, driver layers | 🟢 Medium |
| **26** | **Flyweight — Sharing Fine-Grained Objects** | Flyweight | Memory optimization, character rendering | 🟢 Lower |

---

## Volume 6 — Remaining Behavioral Patterns (4 papers)

Less common but complete the GoF coverage.

| # | Title | GoF Pattern | Priority |
|---|-------|------------|---------|
| **27** | **Mediator — Decoupling Components Through a Hub** | Mediator | 🟡 High (event buses) |
| **28** | **Iterator — Traversing Collections Your Way** | Iterator | 🟢 Medium |
| **29** | **Memento — Capturing and Restoring State** | Memento | 🟢 Medium (undo history) |
| **30** | **Interpreter — Building Grammar-Based Systems** | Interpreter | 🟢 Lower |

---

## Total Scope

| Category | Count | Status |
|----------|-------|--------|
| Framework papers (Vol 1) | 3 | ✅ Done |
| Behavioral patterns (Vol 2) | 7 | 6 done, 1 planned |
| Enterprise patterns (Vol 3) | 3 | 2 done, 1 planned |
| Creational + Meta (Vol 4) | 7 | 5 done, 2 planned |
| Structural patterns (Vol 5) | 7 | 0 done — all planned |
| Remaining behavioral (Vol 6) | 4 | 0 done — all planned |
| **Total** | **31** | **15 done, 16 planned** |

> Note: 31 total because this series includes non-GoF enterprise patterns (Specification, Lookup Tables) and meta papers (Anti-Patterns, Decision Tree, 2026 Relevance) that add genuine value beyond the 23 GoF patterns.

---

## Next 4 Papers to Write (Priority Order)

1. **Paper 16 — Observer** (event-driven architecture, RxJava, reactive patterns)
2. **Paper 18 — Decorator** (Java I/O streams, Spring AOP, middleware wrapping)
3. **Paper 19 — Proxy** (lazy loading, caching proxy, security proxy, gRPC stubs)
4. **Paper 20 — Adapter** (legacy integration, API wrapping)
