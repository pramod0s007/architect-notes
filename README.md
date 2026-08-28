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
| [22](volume-6-clean-code-principles/paper-22-liskov-substitution-principle/) | [Liskov Substitution Principle](https://medium.com/@replytopramods.aws/liskov-substitution-principle-012dae842c65) | document hierarchy, shape composition |
| [23](volume-6-clean-code-principles/paper-23-interface-segregation-principle/) | [Interface Segregation Principle](https://medium.com/@replytopramods.aws/interface-segregation-principle-576d43850bb4) | notification channels, media player |
| [24](volume-6-clean-code-principles/paper-24-dependency-inversion-principle/) | [Dependency Inversion Principle](https://medium.com/@replytopramods.aws/dependency-inversion-principle-5265cc4e5ae6) | order service, notification service, report service |
| [25](volume-6-clean-code-principles/paper-25-dry-dont-repeat-yourself/) | [DRY — Don't Repeat Yourself](https://medium.com/@replytopramods.aws/dry-dont-repeat-yourself-2beda5a9c810) | discount calculator, address validator |
| [26](volume-6-clean-code-principles/paper-26-kiss-keep-it-simple/) | [KISS — Keep It Simple](https://medium.com/@replytopramods.aws/kiss-keep-it-simple-b562f17ec689) | product search, config loader, HTTP handler |
| [27](volume-6-clean-code-principles/paper-27-yagni-you-arent-gonna-need-it/) | [YAGNI — You Aren't Gonna Need It](https://medium.com/@replytopramods.aws/yagni-you-arent-gonna-need-it-e80785e3ba82) | discount evolution, event publisher, user repository |

### [Volume 7 — Design Principles](volume-7-design-principles/)
The OO fundamentals underneath every named pattern — how much a module should hide, how much it should know about its neighbors, and what to build instead of a subclass.

| Paper | Title | Examples |
|-------|-------|---------|
| [28](volume-7-design-principles/paper-28-abstraction/) | Abstraction — Choosing the Right Level | payment gateway (boolean vs. SettlementStatus, capability split) |
| [29](volume-7-design-principles/paper-29-coupling/) | Coupling — Hidden Dependencies and How They Spread | pricing engine, invoice generator, shipping quote, shared config |
| [30](volume-7-design-principles/paper-30-composition-over-inheritance/) | Composition Over Inheritance | report generator (format x encryption x compression x signing), fleet vehicle capabilities |

### [Volume 8 — Concurrency Patterns](volume-8-concurrency-patterns/)
Real bugs that only appear under concurrent load — reproduced on purpose, then fixed with a proof for why the fix holds.

| Paper | Title | Examples |
|-------|-------|---------|
| [31](volume-8-concurrency-patterns/paper-31-deadlock/) | Deadlock — The Bug That Freezes Production | funds transfer (reproducible circular wait, lock ordering fix, tryLock+backoff fix) |
| [32](volume-8-concurrency-patterns/paper-32-reactor-pattern/) | Reactor Pattern — Non-Blocking I/O Done Right | thread-per-connection cost measurement, single-threaded Selector-based reactor |
| [33](volume-8-concurrency-patterns/paper-33-sync-primitives/) | Sync Primitives — Locks, Semaphores, and When They Lie to You | request-in-flight counter (plain vs. volatile vs. synchronized vs. AtomicInteger), per-field-lock invariant bug |
| [34](volume-8-concurrency-patterns/paper-34-actor-pattern/) | Actor Pattern — Concurrency Without Shared State | same Paper 31 transfer rebuilt as actors, single-threaded-executor self-deadlock |
| [35](volume-8-concurrency-patterns/paper-35-task-vs-data-parallelism/) | Task vs Data Parallelism — Decision Tree | image pipeline: wrong data-parallelization (silently wrong output), correct data parallelism (1000 images), correct task parallelism (4-stage pipeline) |
| [36](volume-8-concurrency-patterns/paper-36-io-parallelism/) | I/O Parallelism — Thread-per-Request vs Event Loop | downstream fan-out pool sizing (pool 8 vs 64, measured ~7x), async alternative (4 threads, 400 in-flight calls) |
| [37](volume-8-concurrency-patterns/paper-37-cancellation-and-graceful-shutdown/) | Cancellation & Graceful Shutdown | un-cancellable worker (measured), cooperative fix, interrupt-hiding demonstration |

### [Volume 9 — Distributed Systems & Resilience](volume-9-distributed-systems-resilience/)
One running example — an order-fulfillment system — evolved across all 8 papers. Each paper's fix creates the next paper's problem, from monolith to distributed system.

| Paper | Title | Examples |
|-------|-------|---------|
| [44](volume-9-distributed-systems-resilience/paper-44-strangler-fig/) | Strangler Fig | order-fulfillment monolith extracting a payment provider via a routing facade, duplicated-rule drift demonstration |
| [45](volume-9-distributed-systems-resilience/paper-45-api-gateway/) | API Gateway | two clients drifting on the same failure message (measured), gateway fix, business-logic-in-gateway reconciliation mismatch |
| [46](volume-9-distributed-systems-resilience/paper-46-distributed-monolith/) | Distributed Monolith | shared-table + synchronous chain latency (measured ~3000ms), data-ownership fix that deliberately doesn't fix latency |
| [47](volume-9-distributed-systems-resilience/paper-47-sidecar-and-ambassador/) | Sidecar & Ambassador | three services with three different retry policies (measured inconsistent outcome), shared sidecar fix, bespoke-ambassador overcorrection |
| [48](volume-9-distributed-systems-resilience/paper-48-circuit-breaker-and-bulkhead/) | Circuit Breaker & Bulkhead | shared-pool starvation from a dead dependency (measured), circuit-breaker-alone vs bulkhead-alone vs both (measured separately), shared-breaker-instance mistake |
| [49](volume-9-distributed-systems-resilience/paper-49-saga-pattern/) | Saga Pattern | three correct service calls, one wrong aggregate outcome (measured), compensations accumulated in reverse, per-branch compensation that silently drops a step |
| [50](volume-9-distributed-systems-resilience/paper-50-outbox-pattern/) | Outbox Pattern | dual-write problem: local write succeeds, event silently lost (measured), outbox + retrying relay, relay that marks published before confirming |
| [51](volume-9-distributed-systems-resilience/paper-51-idempotency/) | Idempotency | same event redelivered, naive handler double-ships (measured), stable-eventId dedup, dedup key regenerated per-call that never matches |

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
