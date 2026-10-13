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

## Series — 168 Papers

**Medium publishing status (confirmed October 13, 2026):** Papers 1–38 are published and linked below. Papers 39–168 are authored but not yet published: **38 published, 130 remaining**. Next: Paper 39, Service Discovery.

### [Volume 1 — Thinking Like an Architect](volume-1-thinking-like-an-architect/)
Foundation papers. Read these before anything else.

| Paper | Title |
|-------|-------|
| [01](volume-1-thinking-like-an-architect/paper-01-why-memorizing-design-patterns-is-holding-you-back/) | [Why Memorizing Design Patterns Is Holding You Back](https://medium.com/@replytopramods.aws/why-memorizing-design-patterns-is-holding-you-back-6b6dfa8d7d7c) |
| [02](volume-1-thinking-like-an-architect/paper-02-the-four-architectural-buckets/) | [The Four Architectural Buckets](https://medium.com/@replytopramods.aws/the-four-architectural-buckets-73cd22f3c33c) |
| [03](volume-1-thinking-like-an-architect/paper-03-the-death-of-if-else/) | [The Death of if-else](https://medium.com/@replytopramods.aws/the-death-of-if-else-8044410a1ef8) |

### [Volume 2 — Behavioral Design](volume-2-behavioral-design/)
The most commonly needed patterns in production systems.

| Paper | Title | Examples |
|-------|-------|---------|
| [04](volume-2-behavioral-design/paper-04-strategy-pattern-through-real-refactoring/) | [Strategy Pattern Through Real Refactoring](https://medium.com/@replytopramods.aws/strategy-pattern-through-real-refactoring-25f41134d26c) | encryption, payment gateway, pricing engine |
| [05](volume-2-behavioral-design/paper-05-state-pattern-through-a-stopwatch/) | [State Pattern Through a StopWatch](https://medium.com/@replytopramods.aws/state-pattern-through-a-stopwatch-1d44c670fb32) | stopwatch, order processing |
| [06](volume-2-behavioral-design/paper-06-command-pattern-through-banking-systems/) | [Command Pattern Through Banking Systems](https://medium.com/@replytopramods.aws/command-pattern-through-banking-systems-a50491b11df9) | banking, document editor, job scheduler |
| [07](volume-2-behavioral-design/paper-07-visitor-pattern-without-uml/) | [Visitor Pattern Without UML](https://medium.com/@replytopramods.aws/visitor-pattern-without-uml-0ec8915fa2a5) | collision engine, document processor |
| [08](volume-2-behavioral-design/paper-08-lookup-tables-vs-polymorphism/) | [Lookup Tables vs Polymorphism](https://medium.com/@replytopramods.aws/lookup-tables-vs-polymorphism-51befea88a26) | collision engine (table approach) |

### [Volume 3 — Enterprise Patterns](volume-3-enterprise-patterns/)
Patterns for business rules and multi-step pipelines.

| Paper | Title | Examples |
|-------|-------|---------|
| [09](volume-3-enterprise-patterns/paper-09-specification-pattern/) | [Specification Pattern](https://medium.com/@replytopramods.aws/specification-pattern-5bbe84d9c56e) | product search, loan eligibility |
| [10](volume-3-enterprise-patterns/paper-10-chain-of-responsibility/) | [Chain of Responsibility](https://medium.com/@replytopramods.aws/chain-of-responsibility-baf650a0e7e4) | request pipeline, API security pipeline |

### [Volume 4 — Creational Patterns + Architect-Level Thinking](volume-4-architect-level-thinking/)
How objects are built, and meta-level pattern judgment.

| Paper | Title | Examples |
|-------|-------|---------|
| [11](volume-4-architect-level-thinking/paper-11-builder-pattern/) | [Builder Pattern](https://medium.com/@replytopramods.aws/builder-pattern-747bf70e4b0e) | HTTP request, database config, search request |
| [12](volume-4-architect-level-thinking/paper-12-factory-pattern/) | [Factory Pattern](https://medium.com/@replytopramods.aws/factory-pattern-0a2ea0e58d1e) | notification factory, storage factory |
| [13](volume-4-architect-level-thinking/paper-13-when-patterns-become-anti-patterns/) | [When Patterns Become Anti-Patterns](https://medium.com/@replytopramods.aws/when-patterns-become-anti-patterns-309e57533027) | — |
| [14](volume-4-architect-level-thinking/paper-14-pattern-selection-decision-tree/) | [Pattern Selection Decision Tree](https://medium.com/@replytopramods.aws/pattern-selection-decision-tree-f640d5246fd4) | — |
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
| [28](volume-7-design-principles/paper-28-abstraction/) | [Abstraction — Choosing the Right Level](https://medium.com/@replytopramods.aws/abstraction-choosing-the-right-level-a8999030ac5a) | payment gateway (boolean vs. SettlementStatus, capability split) |
| [29](volume-7-design-principles/paper-29-coupling/) | [Coupling — Hidden Dependencies and How They Spread](https://medium.com/@replytopramods.aws/coupling-hidden-dependencies-and-how-they-spread-c5bdbe15ba08) | pricing engine, invoice generator, shipping quote, shared config |
| [30](volume-7-design-principles/paper-30-composition-over-inheritance/) | [Composition Over Inheritance](https://medium.com/@replytopramods.aws/composition-over-inheritance-b5418fa73c11) | report generator (format x encryption x compression x signing), fleet vehicle capabilities |

### [Volume 8 — Concurrency Patterns](volume-8-concurrency-patterns/)
Real bugs that only appear under concurrent load — reproduced on purpose, then fixed with a proof for why the fix holds.

| Paper | Title | Examples |
|-------|-------|---------|
| [31](volume-8-concurrency-patterns/paper-31-deadlock/) | [Deadlock — The Bug That Freezes Production](https://medium.com/@replytopramods.aws/deadlock-the-bug-that-freezes-production-d2a17ddc8e7e) | funds transfer (reproducible circular wait, lock ordering fix, tryLock+backoff fix) |
| [32](volume-8-concurrency-patterns/paper-32-reactor-pattern/) | [Reactor Pattern — Non-Blocking I/O Done Right](https://medium.com/@replytopramods.aws/reactor-pattern-non-blocking-i-o-done-right-c56687b42114) | thread-per-connection cost measurement, single-threaded Selector-based reactor |
| [33](volume-8-concurrency-patterns/paper-33-sync-primitives/) | [Sync Primitives — Locks, Semaphores, and When They Lie to You](https://medium.com/@replytopramods.aws/sync-primitives-locks-semaphores-and-when-they-lie-to-you-c52c82e97955) | request-in-flight counter (plain vs. volatile vs. synchronized vs. AtomicInteger), per-field-lock invariant bug |
| [34](volume-8-concurrency-patterns/paper-34-actor-pattern/) | [Actor Pattern — Concurrency Without Shared State](https://medium.com/@replytopramods.aws/actor-pattern-concurrency-without-shared-state-ea0fa60af1df) | same Paper 31 transfer rebuilt as actors, single-threaded-executor self-deadlock |
| [35](volume-8-concurrency-patterns/paper-35-task-vs-data-parallelism/) | [Task vs Data Parallelism — Decision Tree](https://medium.com/@replytopramods.aws/task-vs-data-parallelism-decision-tree-452d413ff49f) | image pipeline: wrong data-parallelization (silently wrong output), correct data parallelism (1000 images), correct task parallelism (4-stage pipeline) |
| [36](volume-8-concurrency-patterns/paper-36-io-parallelism/) | [I/O Parallelism — Thread-per-Request vs Event Loop](https://medium.com/@replytopramods.aws/i-o-parallelism-thread-per-request-vs-event-loop-c5a4c6d96cae) | downstream fan-out pool sizing (pool 8 vs 64, measured ~7x), async alternative (4 threads, 400 in-flight calls) |
| [37](volume-8-concurrency-patterns/paper-37-cancellation-and-graceful-shutdown/) | [Cancellation & Graceful Shutdown](https://medium.com/@replytopramods.aws/cancellation-graceful-shutdown-e765116a60c3) | un-cancellable worker (measured), cooperative fix, interrupt-hiding demonstration |

### [Volume 8b — Networking & RPC Fundamentals](volume-8b-networking-rpc-fundamentals/)
A short bridge volume between Volume 8 and Volume 9. Each paper uses an independent example, evolved across 3 steps: a naive/failing design → a fix that solves one problem while exposing the next → the real fix.

| Paper | Title | Examples |
|-------|-------|---------|
| [38](volume-8b-networking-rpc-fundamentals/paper-38-load-balancing-strategies/) | [Load Balancing Strategies](https://medium.com/@replytopramods.aws/load-balancing-strategies-1ff845eeb032) | round robin vs. mismatched backend capacity (measured), weighted round robin, dynamic least-load under runtime capacity degradation |
| [39](volume-8b-networking-rpc-fundamentals/paper-39-service-discovery/) | Service Discovery | static locator surviving a redeploy (measured failure), heartbeat/TTL registry, health-checked registry |
| [40](volume-8b-networking-rpc-fundamentals/paper-40-rpc-vs-rest/) | RPC vs REST | string-keyed field rename silently breaking a reader, schema-evolution-safe alternative |
| [41](volume-8b-networking-rpc-fundamentals/paper-41-rate-limiting/) | Rate Limiting | fixed-window boundary burst (measured 2x rate), sliding-window log fix, per-instance drift across 3 load-balanced instances (measured 3x global cap), shared/centralized limiter |
| [42](volume-8b-networking-rpc-fundamentals/paper-42-caching/) | Caching | no-expiration permanent staleness, TTL cache causing a 20-thread cache stampede (measured 21 loads), single-flight/request-coalescing fix (measured 2 loads), including a real TOCTOU concurrency bug found and fixed during development |
| [43](volume-8b-networking-rpc-fundamentals/paper-43-message-queues/) | Message Queues | direct synchronous call losing a message on failure, at-most-once queue still losing a message mid-crash, at-least-once queue with redelivery-caused duplicate charge, idempotent consumer fix |

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

### [Volume 10 — System Design Blueprints, Evaluated Like a Principal Architect](volume-10-system-design-blueprints/)
Seven classic system design interview questions, each paired with one formal architecture-evaluation method instead of a whiteboard vibe check.

| Paper | System | Eval Method | Examples |
|-------|--------|-------------|---------|
| [52](volume-10-system-design-blueprints/paper-52-url-shortener/) | URL Shortener | QAS | random-retry attempts climbing with keyspace fill (measured), centralized vs block-allocated counter (measured ~90x), birthday-paradox collision trap (measured) |
| [53](volume-10-system-design-blueprints/paper-53-rate-limiter/) | Rate Limiter | ADR | fixed-window boundary burst (measured 2x), token bucket fix (measured exact), per-node token buckets with no shared state (measured 5x over budget) |
| [54](volume-10-system-design-blueprints/paper-54-ticket-booking-flash-sale/) | Ticket Booking (flash sale) | ATAM | no-lock overselling (measured 100% every run), optimistic vs pessimistic at moderate load (measured ~30-40% gap), same pair at extreme load (measured gap vanishes — a real sensitivity point) |
| [55](volume-10-system-design-blueprints/paper-55-ride-booking-dispatch/) | Ride Booking Dispatch | CBAM | naive O(n) driver scan cost growing with platform size (measured), grid-bucketed dispatch (measured ~24x), same code with a mistuned cell size (measured benefit vanishes) |
| [56](volume-10-system-design-blueprints/paper-56-messaging-app/) | Messaging App | 4+1 Views | naive arrival-order inbox + receipt tracker (measured scrambled order, regressed status), server-sequence fix (measured correct), client-clock ordering (measured a 5s skew reordering a conversation) |
| [57](volume-10-system-design-blueprints/paper-57-social-feed-system/) | Social Feed System | SAAM | fan-out-on-write cost growing unbounded (measured), hybrid push/pull fix (measured flat), pure fan-out-on-read solving one scenario while breaking an unchecked one (measured) |
| [58](volume-10-system-design-blueprints/paper-58-cloud-file-storage/) | Cloud File Storage | QAS | no-dedup 5x storage cost (measured), content-defined chunking re-syncing after an edit (measured 1/517 new chunks), fixed-size chunking's boundary-shift failure (measured 782/782 new chunks) |

### [Volume 11 — Data Architecture Patterns](volume-11-data-architecture-patterns/)
One running example — an account ledger — evolved across three data-architecture patterns. Each paper's fix creates the next paper's problem: read/write contention → the need for full history → a single node running out of room.

| Paper | Title | Examples |
|-------|-------|---------|
| [59](volume-11-data-architecture-patterns/paper-59-cqrs/) | CQRS | shared-lock write latency exploding under read contention (measured ~300-400ms), separate-lock fix (measured 0ms), async propagation reintroducing a read-your-own-writes gap (measured) |
| [60](volume-11-data-architecture-patterns/paper-60-event-sourcing/) | Event Sourcing | balance-only ledger with no memory of its own history (measured), event log answering an unanticipated report (measured), balance-snapshot ledger losing transaction type permanently (measured) |
| [61](volume-11-data-architecture-patterns/paper-61-sharding/) | Sharding | single-node throughput ceiling for unrelated accounts (measured ~650-730ms), account-ID sharding fix (measured ~56ms, ~12x), sharding by a poorly-distributed region key (measured statistically identical to no sharding) |

### [Volume 12 — Production Engineering & Observability](volume-12-production-engineering-observability/)
Operational practice, not architecture — standalone papers on what gets measured, what pages a human, and how a team decides an incident is over. Each still ships a runnable Java demonstration of the underlying mechanism.

| Paper | Title | Examples |
|-------|-------|---------|
| [62](volume-12-production-engineering-observability/paper-62-slos-error-budgets/) | SLOs & Error Budgets | fixed-threshold alert missing a budget-exhausting slow burn (measured), long-window burn-rate fix catching it early (measured), short-window burn rate false-alarming on a harmless blip (measured) |
| [63](volume-12-production-engineering-observability/paper-63-alerting-symptom-vs-cause/) | Alerting: Symptom-Based vs. Cause-Based | CPU-based alerting false-positive and false-negative (measured), user-facing-error-rate fix (measured correct on both), symptom measured at the wrong point in the request path (measured missing a 3s customer wait) |
| [64](volume-12-production-engineering-observability/paper-64-blameless-postmortems/) | Blameless Postmortems | single-root-cause postmortem missing 2 of 3 real factors (measured, incident recurs), prioritized contributing-factors fix (measured, no recurrence), unprioritized factor list still missing the critical fix (measured, incident recurs anyway) |
| [65](volume-12-production-engineering-observability/paper-65-observability-signals/) | Observability: Metrics, Logs, and Traces | metrics-only p99 correctly elevated but structurally unable to attribute a service (measured), trace with spans fix (measured exact culprit and duration), uncorrelated logs leaving 3 concurrent requests equally plausible (measured) |

### [Volume 13 — AI System Design I: RAG & Vector Search](volume-13-ai-system-design-retrieval/)
Retrieval-augmented generation as a system design problem, not an API-calling exercise — no LLM API calls anywhere, pure algorithmic Java simulating each mechanism (chunking, indexing, embedding comparison, fusion, reranking, context assembly) deterministically.

| Paper | Title | Examples |
|-------|-------|---------|
| [66](volume-13-ai-system-design-retrieval/paper-66-chunking-strategy/) | Chunking Strategy | fixed-size chunk boundary splitting the answer-bearing sentence (measured, retrieval fails), sentence-aware chunking fix (measured, retrieval succeeds), overlap added without fixing the defective chunk (measured, 67% more chunks, same failure) |
| [67](volume-13-ai-system-design-retrieval/paper-67-vector-index-tradeoffs/) | Vector Index Tradeoffs | brute-force exact search scaling linearly with N (measured), IVF nprobe=2 matching brute force at 60% of the cost (measured), IVF nprobe=1 missing a boundary neighbor by 10x (measured) |
| [68](volume-13-ai-system-design-retrieval/paper-68-embedding-staleness/) | Embedding Staleness | cross-model query-vs-document comparison producing a confident, wrong top result (measured 0.99 similarity), full re-embed fix (measured correct result), migrate-on-edit still leaving unedited documents stale (measured) |
| [69](volume-13-ai-system-design-retrieval/paper-69-hybrid-search/) | Hybrid Search | pure vector search missing an exact product code the user typed (measured), bounded same-scale keyword boost fix (measured correct by the intended margin), unnormalized raw keyword counts letting a spammy document dominate (measured) |
| [70](volume-13-ai-system-design-retrieval/paper-70-retrieval-reranking/) | Retrieval Reranking | bi-encoder ranking the truly best answer #2 instead of #1 (measured), cross-encoder joint rerank fix (measured correct), a bigger bi-encoder reproducing the identical wrong top-1 (measured) |
| [71](volume-13-ai-system-design-retrieval/paper-71-rag-failure-modes/) | RAG Failure Modes | document-order context assembly cutting the most relevant chunk entirely under budget (measured), relevance-order assembly fix (measured), a generous budget letting a stale chunk contradict the correct answer undetected (measured) |

### [Volume 14 — AI System Design II: Serving, Cost & Agentic Systems](volume-14-ai-system-design-serving/)
Serving an LLM system in production — batching, caching, cost-aware routing, context budgeting, agentic tool-call reliability, and output validation. No LLM API calls anywhere, pure algorithmic Java simulating each mechanism deterministically.

| Paper | Title | Examples |
|-------|-------|---------|
| [72](volume-14-ai-system-design-serving/paper-72-llm-request-batching/) | LLM Request Batching | no batching paying 91% pure overhead (measured), batch size OR timeout trigger giving 5.5x throughput with zero starvation (measured), batch-size-only trigger starving requests indefinitely under sparse traffic (measured) |
| [73](volume-14-ai-system-design-serving/paper-73-kv-cache-reuse/) | KV-Cache Reuse | no caching growing cost with the square of conversation length (measured), content-keyed cache giving 3.24x fewer tokens processed (measured), length-keyed cache colliding two unrelated conversations (measured) |
| [74](volume-14-ai-system-design-serving/paper-74-model-cascading/) | Model Cascading | always-expensive routing at 100% correctness and 100% cost (measured), category-based routing at 48% less cost with zero correctness lost (measured), confidence-based routing cheaper still but confidently wrong on the query that mattered (measured) |
| [75](volume-14-ai-system-design-serving/paper-75-context-window-budgeting/) | Context Window Budgeting | tail truncation cutting the user's entire question (measured), reserve-essentials-drop-oldest-history fix (measured both essentials intact), never-truncate-history deleting the system prompt's safety constraint instead (measured) |
| [76](volume-14-ai-system-design-serving/paper-76-agentic-tool-calling/) | Agentic Tool-Calling Reliability | fresh idempotency key per retry producing 2 orders for 1 action (measured), same-key-reuse fix giving exactly 1 order (measured), never-retrying reporting a successfully created order as failed (measured) |
| [77](volume-14-ai-system-design-serving/paper-77-output-guardrails/) | Output Guardrails | unvalidated negative refund amount becoming a real customer charge (measured), schema/range validation rejecting the malformed request (measured), auto-repair silently processing the corrupted request with zero visibility (measured) |

### [Volume 15 — Consensus & Distributed Coordination](volume-15-consensus-distributed-coordination/)
Coordinating many nodes that all need to agree on one thing at a time — leader election, quorum reads/writes, distributed locking, split-brain, replication lag, and consistent snapshots. Every failure in this volume looks, from inside any single node, like the system is working exactly as designed.

| Paper | Title | Examples |
|-------|-------|---------|
| [78](volume-15-consensus-distributed-coordination/paper-78-leader-election/) | Leader Election | first-to-declare election producing 2 simultaneous leaders under a partition (measured), majority-vote election giving exactly 1 leader (measured), a lease without a fencing token letting a stale write land after leadership moved on (measured) |
| [79](volume-15-consensus-distributed-coordination/paper-79-quorum-reads-writes/) | Quorum Reads and Writes | W=1/R=1 returning a stale read right after an acknowledged write (measured), W+R>N with version comparison correctly returning the latest write (measured), a numerically valid quorum still returning stale data because the reader used arrival order instead of version comparison (measured) |
| [80](volume-15-consensus-distributed-coordination/paper-80-redlock-critique/) | The Redlock Critique | one lock server's fast clock double-granting a majority lock to two clients (measured), a fencing token at the resource rejecting the stale client's write regardless (measured), a 5x shorter TTL reproducing the identical double-grant at a smaller scale (measured) |
| [81](volume-15-consensus-distributed-coordination/paper-81-split-brain/) | Split-Brain | a failover controller promoting a standby on health-check failure alone, producing two live primaries (measured), fencing over an independent channel before promoting (measured), a 3x longer timeout reproducing the identical split-brain, just delayed (measured) |
| [82](volume-15-consensus-distributed-coordination/paper-82-replication-lag/) | Replication Lag | round-robin routing losing a user's own write on the very next read (measured), routing by required replication position with primary fallback guaranteeing freshness (measured), pinning the writer's session to the primary fixing it for the writer but not a different reader of the same data (measured) |
| [83](volume-15-consensus-distributed-coordination/paper-83-distributed-snapshotting/) | Distributed Snapshotting | independent local snapshots losing a message in flight entirely (measured), a marker on the real FIFO channel producing a snapshot that exactly matches reality (measured), a guessed safety delay reproducing the identical lost-message bug under real timing variance (measured) |

---

### [Volume 16 — Streaming & Real-Time Data Architecture](volume-16-streaming-realtime-data/)
Turning a table into a stream, a stream into a table, and keeping both correct under partitioning, redelivery, backpressure, and out-of-order arrival. Every paper in this volume traces back to the same two concerns: who owns the truth, and whose order is being preserved.

| Paper | Title | Examples |
|-------|-------|---------|
| [84](volume-16-streaming-realtime-data/paper-84-partitioning-strategy/) | Partitioning Strategy | a low-cardinality region key concentrating 24 of 30 events on one partition (measured), a high-cardinality userId key giving an exact 10/10/10 split 2.4x faster (measured), round-robin partitioning achieving perfect balance while applying one user's two updates out of order (measured) |
| [85](volume-16-streaming-realtime-data/paper-85-exactly-once-semantics/) | Exactly-Once Semantics | a consumer with no memory double-applying a redelivered charge (measured), deduplication by messageId skipping the redelivery while still applying a genuinely new event (measured), deduplication by content hash silently dropping a real, distinct charge (measured) |
| [86](volume-16-streaming-realtime-data/paper-86-change-data-capture/) | Change Data Capture | polling current table state capturing 1 of 5 real writes, with a deleted row leaving no event at all (measured), tailing the transaction log capturing all 5 writes exactly, deletes included (measured), compacting a correctly-tailed log by key still losing an intermediate transition (measured) |
| [87](volume-16-streaming-realtime-data/paper-87-backpressure/) | Backpressure | an unbounded queue's peak size already exceeding a safe memory budget after 12 ticks (measured), a bounded queue that blocks the producer delivering zero data loss at the cost of throughput (measured), a bounded queue that drops on full losing 4 of 12 real events while looking healthier on every metric (measured) |
| [88](volume-16-streaming-realtime-data/paper-88-windowing-watermarks/) | Windowing and Watermarks | bucketing by arrival time shifting 400c of revenue into the wrong minute (measured), event-time bucketing with an event-time-driven watermark correctly including a late arrival (measured), the correct bucketing key paired with an arrival-driven watermark still dropping the same late arrival (measured) |
| [89](volume-16-streaming-realtime-data/paper-89-stream-table-duality/) | Stream-Table Duality | a table updated via a separate write path silently drifting from its own stream (measured), a table defined purely as a fold of the stream rebuilding identically after a simulated crash (measured), a single-source table still drifting when the fold applies events out of true offset order (measured) |

---

### [Volume 17 — Security Architecture for Staff Engineers](volume-17-security-architecture/)
Correctness under an adversary actively looking for the one place a system's logic doesn't match its intent. Every failure in this volume is invisible to a review that only checks whether the right kind of control is present, without verifying it actually delivers the specific property it claims to.

| Paper | Title | Examples |
|-------|-------|---------|
| [90](volume-17-security-architecture/paper-90-authentication-vs-authorization/) | Authentication vs Authorization | a genuinely authenticated user reading another user's data because no authorization check ever ran (measured), authorization against the session's own server-issued identity correctly denying cross-user access (measured), an authorization check that matches successfully yet protects nothing because it trusts a client-claimed identity (measured) |
| [91](volume-17-security-architecture/paper-91-broken-object-level-authorization/) | Broken Object-Level Authorization | a read correctly denied while an update to the identical resource, by the identical unauthorized user, succeeds completely (measured), a single shared ownership check denying both operations identically (measured), a check centralized at the network edge bypassed entirely by a direct internal caller (measured) |
| [92](volume-17-security-architecture/paper-92-secrets-management/) | Secrets Management | a key read straight out of source code authorizing a real production charge (measured), a leaked development secret failing to authorize a production charge because environments were never sharing one identity (measured), a legitimate in-flight caller failing a real charge the instant a secret rotates with no grace period (measured) |
| [93](volume-17-security-architecture/paper-93-cryptographic-misuse/) | Cryptographic Misuse | two accounts with the identical password producing byte-for-byte identical, comparable hashes with no salt (measured), a unique salt per account eliminating that comparability while iteration adds a real, measured per-guess cost (measured), a single salt shared by every account reintroducing the identical comparability failure despite salting and iteration both being present (measured) |
| [94](volume-17-security-architecture/paper-94-injection-trust-boundary/) | Injection and the Trust Boundary | a single-order lookup returning every order in the system via one injected OR condition (measured), the identical malicious input returning zero records once compared as data instead of parsed as syntax (measured), a working denylist filter defeated entirely by changing one word's letter case (measured) |
| [95](volume-17-security-architecture/paper-95-defense-in-depth/) | Defense in Depth | one flawed authorization check producing total compromise with nothing else in place to catch it (measured), a second, independently-implemented check catching exactly what the first layer's real bug let through (measured), three identically-implemented checks at three named layers all failing identically on the same input (measured) |

---

### [Volume 18 — Multi-Tenant & SaaS Architecture](volume-18-multi-tenant-saas-architecture/)
Protecting one tenant of a shared system from another tenant who isn't attacking anything, just using the product normally at a scale that happens to be enormous. Every failure in this volume is a shared resource that works correctly for the system as a whole while quietly failing one specific tenant.

| Paper | Title | Examples |
|-------|-------|---------|
| [96](volume-18-multi-tenant-saas-architecture/paper-96-noisy-neighbor-isolation/) | Noisy Neighbor Isolation | a small tenant starved to zero requests because an unrelated tenant's burst consumed the entire shared pool first (measured), a guaranteed per-tenant quota with unused capacity reclaimed protecting the small tenant while fully utilizing the system (measured), a rigid fixed quota protecting the small tenant while leaving 40% of capacity idle and a legitimate tenant throttled (measured) |
| [97](volume-18-multi-tenant-saas-architecture/paper-97-tenant-schema-migration/) | Tenant Onboarding and Schema Migration | 3 of 5 tenants double-processed after a crash forces a full restart with no checkpoint (measured), a per-tenant checkpoint resuming exactly where a crash left off with zero reapplication (measured), a per-batch checkpoint still double-processing 1 of 5 tenants because its granularity doesn't match the real unit of idempotency (measured) |
| [98](volume-18-multi-tenant-saas-architecture/paper-98-tenant-configuration-feature-flags/) | Tenant Configuration and Feature Flags | a global flag intended for one tenant's beta instantly enabling it for two unrelated tenants (measured), a data-driven per-tenant map isolating one tenant's setting completely (measured), correctly isolated but hardcoded tenant logic requiring a full source code change and deploy to onboard one more tenant (measured) |
| [99](volume-18-multi-tenant-saas-architecture/paper-99-cross-tenant-caching/) | Cross-Tenant Caching | a cache key built from a resource ID alone serving one tenant's data to a different tenant on its very first request (measured), a tenant-scoped cache key eliminating the collision entirely (measured), correctly-scoped reads paired with unscoped invalidation forcing two unrelated tenants into unnecessary database refetches (measured) |
| [100](volume-18-multi-tenant-saas-architecture/paper-100-tenant-aware-observability/) | Tenant-Aware Observability | a tenant's complete outage diluted into a healthy-looking 5% aggregate error rate that never triggers an alert (measured), per-tenant tagged metrics correctly surfacing and alerting on the identical outage (measured), tenant-tagged metrics still invisible because the backend's finite series capacity was exhausted before the affected tenant's series could register (measured) |

---

### [Volume 19 — Platform Engineering & Developer Experience](volume-19-platform-engineering-developer-experience/)
Protecting the engineers building the system, where the shared resource is an engineering team's own time. Every mechanism in this volume fails the same way when it's designed around the common case with no honest answer for the case that doesn't fit.

| Paper | Title | Examples |
|-------|-------|---------|
| [101](volume-19-platform-engineering-developer-experience/paper-101-paved-road-paradox/) | The Paved Road Paradox | a platform supporting one deployment shape rejecting 2 legitimate services outright (measured), a reviewed escape hatch enforcing the same baseline letting all 10 services deploy fully compliant (measured), an unrestricted escape hatch causing 6 of 10 services to defect and compliance to collapse to 4 of 10 (measured) |
| [102](volume-19-platform-engineering-developer-experience/paper-102-ci-flakiness-retry-trap/) | CI Flakiness and the Retry Trap | a real race-condition bug passing a build after 2 failed attempts, indistinguishable from a genuine flake (measured), a documented allowlist protecting the genuine flake while catching the real bug on its only attempt (measured), a shared retry budget still letting the real bug borrow retries meant for the genuine flake and slip through (measured) |
| [103](volume-19-platform-engineering-developer-experience/paper-103-local-prod-parity/) | Local-Prod Parity | identical code succeeding locally and failing against production's real collation behavior (measured), local dev matching the one specific behavior that determines correctness and catching the bug before deploy (measured), a fully faithful but too-slow parity environment bypassed daily for an unfaithful fallback that reintroduces the bug (measured) |
| [104](volume-19-platform-engineering-developer-experience/paper-104-service-ownership-oncall-boundaries/) | Service Ownership and On-Call Boundaries | an alert routed to the visible symptom wasting 15 minutes before the real owner starts investigating (measured), ownership pre-established per alert type letting the correct team start at minute zero (measured), paging every possible team causing diffusion of responsibility that delays real action past even the wrong-team routing (measured) |
| [105](volume-19-platform-engineering-developer-experience/paper-105-golden-path-enforcement/) | Golden Path Enforcement | two independently-tooled services sharing zero common field to correlate the same real request (measured), a shared required trace ID turning cross-service correlation into an exact match (measured), a hard gate with no exception process satisfied by a fake placeholder that falsely merges two unrelated requests (measured) |

---

### [Volume 20 — Performance Engineering & the JVM](volume-20-performance-engineering-jvm/)
Six real JVM performance questions, each showing that the same technique which helps a system under one set of conditions actively hurts it under another — and that measuring, not guessing, is the only way to tell which situation you're in. Most papers in this volume use real measured `System.nanoTime()` wall-clock timing rather than simulation.

| Paper | Title | Examples |
|-------|-------|---------|
| [106](volume-20-performance-engineering-jvm/paper-106-gc-pause-tuning/) | Garbage Collection Pause Tuning | a small heap under real live-set pressure spending 35% of wall time paused for GC (simulated old-gen model, disclosed), the same live set given adequate headroom dropping GC overhead to 12.5% (simulated, disclosed), a heap oversized far beyond the live set eliminating GC pauses entirely but at a memory cost no fixed tuning value can predict for every workload (simulated, disclosed) |
| [107](volume-20-performance-engineering-jvm/paper-107-jit-warmup-cold-start/) | JIT Warmup and Cold Start | a JIT warmed up on one traffic path leaving a second real path running cold and slow the first time it's hit (simulated JIT model, disclosed), warming both real paths before traffic arrives eliminating the cold-path penalty (simulated, disclosed), a warmup script that ran but never actually covered the second path, indistinguishable from a real warmup in a diff (simulated, disclosed) |
| [108](volume-20-performance-engineering-jvm/paper-108-threadlocal-memory-leaks/) | ThreadLocal Memory Leaks | 5 requests on a pooled thread leaving 5 entries permanently resident because nothing ever called remove() (measured), the identical 5 requests leaving 0 residual entries once remove() runs at the true end of the request (measured), remove() genuinely called — from the wrong point in the lifecycle — leaving 0 leaked entries and 5 of 5 requests silently falling back to a degraded default (measured) |
| [109](volume-20-performance-engineering-jvm/paper-109-object-pooling-vs-allocation/) | Object Pooling vs Allocation | 20,000 fresh large-buffer allocations costing 268ms of real, repeated allocation-and-zeroing cost (measured), the identical 20,000 logical uses collapsing to 0ms once the one expensive buffer is reused instead of reallocated (measured), pooling a cheap two-field object through a real synchronized pool measuring roughly 5x slower than just allocating it fresh (measured) |
| [110](volume-20-performance-engineering-jvm/paper-110-thread-pool-sizing/) | Thread Pool Sizing | 64 mostly-blocked tasks queuing behind a pool sized one-thread-per-core, measured at 226ms (measured), the identical tasks collapsing to 66ms once the pool is sized for the wait-to-compute ratio instead (measured), the same oversized pool applied to purely CPU-bound work measuring about 18% slower than a core-matched pool, median of 7 trials (measured) |
| [111](volume-20-performance-engineering-jvm/paper-111-profiling-driven-optimization/) | Profiling-Driven Optimization | optimizing the stage that visually looks slowest barely moving total time, 98ms to 83ms, while the real bottleneck sits untouched (measured), stage-boundary timing correctly attributing 98% of total time to one stage and fixing it for a ~40x improvement (measured), permanent per-item instrumentation on the already-fixed pipeline measuring about 66% slower than stage-level timing, from instrumentation overhead alone (measured) |

---

### [Volume 21 — API Design & Evolution at Scale](volume-21-api-design-evolution-at-scale/)
Five real API-evolution failures, each showing that a change can pass every test its own team wrote and still break a real consumer no one on the provider side was ever told about — and that the fix is never a smarter guess, but a mechanism that removes the need to guess at all.

| Paper | Title | Examples |
|-------|-------|---------|
| [112](volume-21-api-design-evolution-at-scale/paper-112-api-versioning/) | API Versioning | a pricing field silently changing what it represents without changing its name or type, causing a client to overcharge customers with no error at all (verified), an additive-only field addition preserving every existing client's behavior exactly (verified), a genuinely structural field removal correctly requiring a new major version rather than a silent in-place change (verified) |
| [113](volume-21-api-design-evolution-at-scale/paper-113-pagination-at-scale/) | Pagination at Scale | OFFSET/LIMIT pagination duplicating a row when an insert lands between two page requests (verified, duplicate id=5), a single-field `createdAt` cursor fixing the duplicate but silently and permanently dropping a row tied on timestamp with another row (verified, id=6 lost forever), a composite `(createdAt, id)` cursor returning every row exactly once with no possible ties (verified, all 8 events returned) |
| [114](volume-21-api-design-evolution-at-scale/paper-114-idempotency-keys/) | Idempotency Keys | a lost response (not a lost request) triggering a client retry that becomes a genuine second charge with no idempotency key at all (verified, 2×$49.99), an idempotency key checked non-atomically still letting two concurrent requests both pass the check before either records it (verified, still 2×$49.99), an atomic `putIfAbsent` reservation storing the actual charge result and surviving both a concurrent race and a late-arriving duplicate (verified, exactly 1×$49.99 across 3 requests) |
| [115](volume-21-api-design-evolution-at-scale/paper-115-consumer-driven-contract-testing/) | Consumer-Driven Contract Testing | a silently renamed response field passing every test the provider owns while a real, untracked consumer stops generating invoices (verified), a hand-authored integration test that catches the first incident but stays green through an unrelated change to a scenario it never exercises, silently breaking a second untracked consumer (verified), each consumer authoring its own executable contract routed to its own declared scenario, blocking the exact deploy that broke it (verified) |
| [116](volume-21-api-design-evolution-at-scale/paper-116-graphql-n-plus-1/) | GraphQL N+1 | a naive per-field resolver costing 1+N round trips to resolve N objects' nested fields, one per object (verified, 8 posts = 9 round trips), per-request memoization removing round trips only when keys repeat and providing zero benefit on an equally realistic all-distinct-keys feed (verified, 6 out of 6 round trips), a collect-then-batch fetch costing exactly 1 round trip regardless of how much duplication exists in the query (verified, 1 round trip for both feeds) |

---

### [Volume 22 — Caching Architecture Deep Dive](volume-22-caching-architecture-deep-dive/)
Six real caching failures, each showing that fixing the failure a cache was designed for doesn't automatically fix a different failure hiding one layer below it — and that both have to be verified independently, in code, not assumed from the first fix.

| Paper | Title | Examples |
|-------|-------|---------|
| [117](volume-22-caching-architecture-deep-dive/paper-117-cache-stampede/) | Cache Stampede | 1000 keys warmed with an identical fixed TTL all expiring at the same instant, driving 1000 simultaneous recompute calls at once (verified), a jittered TTL spreading those same expiries out and dropping the peak to 115 (verified, ~90% reduction), single-flight/request-coalescing on the recompute itself collapsing concurrent recomputes for the same key down to exactly one backend call (verified) |
| [118](volume-22-caching-architecture-deep-dive/paper-118-cache-eviction-lru-vs-lfu/) | Cache Eviction — LRU vs LFU | a one-time bulk scan of 8 unrelated keys evicting an entire 3-key hot working set under LRU, because "touched once" outranks "touched constantly" (verified), LFU fixing scan resistance completely — 147 of 150 hits versus LRU's 135 of 150 (verified) — but a frequency count with no decay failing to adapt once the real workload genuinely shifts (verified), LFU with periodic decay keeping scan resistance while regaining adaptability (verified) |
| [119](volume-22-caching-architecture-deep-dive/paper-119-write-through-vs-write-behind/) | Write-Through vs Write-Behind | 30 acknowledged write-behind writes vanishing entirely when the process crashes before the buffered flush runs (verified, 0/30 durable), write-through fixing durability completely — 30 of 30 durable across any crash timing (verified) — while its throughput cost was never checked, because the team was solving a crash-loss incident, not auditing latency, a durable local write-ahead log with async flush keeping the same 30/30 durability guarantee without paying synchronous backend latency on every write (verified) |
| [120](volume-22-caching-architecture-deep-dive/paper-120-distributed-cache-consistency/) | Distributed Cache Consistency | a write on one node leaving every other node's cache permanently unaware of the change, with no invalidation at all (verified, 10/10 stale reads forever), a TTL bounding that staleness to a fixed window, but read-your-own-write still failing inside it (verified), write-triggered invalidation broadcast closing the staleness window entirely, including read-your-own-write (verified, 0/10 stale) |
| [121](volume-22-caching-architecture-deep-dive/paper-121-cache-partitioning-consistent-hashing/) | Cache Partitioning & Consistent Hashing | modulo hashing remapping 806 of 1000 keys from a single node addition despite being perfectly balanced at a fixed node count (verified), consistent hashing with one ring point per node cutting that to 300 of 1000 remapped, but leaving a real 6.0x load skew that exists even with zero resizes, because a handful of random ring points can't sample a circle evenly (verified), 2000 virtual points per node dropping the skew to 1.28x while remap cost stays low at 268 of 1000 (verified) |
| [122](volume-22-caching-architecture-deep-dive/paper-122-negative-caching-cache-poisoning/) | Negative Caching & Cache Poisoning | 100 repeated lookups for a missing key costing 100 backend calls with no negative caching at all (verified), negative caching with no invalidation collapsing that to 1 backend call, but poisoning the cache permanently once the record is later created — 10 of 10 subsequent lookups still stale (verified), write-triggered invalidation on create keeping the same 1-backend-call efficiency while cutting stale reads to 0 of 10 (verified) |

---

### [Volume 23 — Database Internals for Architects](volume-23-database-internals-for-architects/)
Seven real database-internals failures, each showing that the "obvious" fix for one cost (write amplification, missing durability, blocked concurrency, a bad plan, a leaked connection, stale replicas, uncoordinated commits) introduces or reveals a different cost that has to be verified on its own — never assumed to be solved as a side effect of the first fix.

| Paper | Title | Examples |
|-------|-------|---------|
| [123](volume-23-database-internals-for-architects/paper-123-btree-vs-lsm-tree/) | B-Tree vs LSM-Tree Indexing | a B-Tree paying 10,686 scattered node writes for 10,000 random-order inserts but only 3 node reads per lookup (verified), an LSM-tree with no Bloom filter cutting those same inserts to 20 sequential flushes while a missing-key lookup jumps to 20 SSTable reads with nothing to rule any of them out (verified), an LSM-tree with a Bloom filter per SSTable keeping the 20 sequential flushes while missing-key lookups drop to 0 actual reads and present-key lookups drop from 20 to 4 (verified) |
| [124](volume-23-database-internals-for-architects/paper-124-write-ahead-log-crash-recovery/) | Write-Ahead Log & Crash Recovery | no durability mechanism at all losing all 50 acknowledged writes on crash, 0 recovered (verified), a write-ahead log that acknowledges before the flush still losing 7 of 47 acknowledged writes (verified), a write-ahead log that acknowledges only after the fsync confirms durability recovering all 47 of 47 writes, 0 lost (verified) |
| [125](volume-23-database-internals-for-architects/paper-125-mvcc-snapshot-isolation/) | MVCC & Snapshot Isolation | no transaction isolation letting a reader combine a pre-transfer and post-transfer balance into a total of 250 that never existed (verified), a global lock making the total correct at 200 but blocking the writer for the reader's entire ~210ms transaction window (verified), MVCC snapshot isolation keeping the same correct total while the writer completes in 0ms with no lock contention at all (verified) |
| [126](volume-23-database-internals-for-architects/paper-126-query-optimizer-cost-based-plans/) | Query Optimizer & Cost-Based Plan Selection | always using the index costing 9505 estimated units on a 950-of-1000-row query versus a full scan's 1000 (verified), always full-scanning instead fixing that query at 1000 but costing 1000 instead of 505 on a different, highly selective 50-of-1000-row query (verified), cost-based selection from real statistics giving both queries their objectively cheaper plan — 1000 and 505 respectively (verified) |
| [127](volume-23-database-internals-for-architects/paper-127-connection-pooling-pool-exhaustion/) | Connection Pooling & Pool Exhaustion | no pooling letting only 5 of a burst of 12 requests through a hard 5-connection database limit, 7 refused outright (verified), a pool with a connection leak exhausting all 5 connections after 5 ordinary requests, timing out a 6th (verified), a pool with guaranteed release via try/finally serving 20 requests (4 deliberately failing) on the same 5 connections, ending with all 5 back and available (verified) |
| [128](volume-23-database-internals-for-architects/paper-128-replication-lag-read-your-writes/) | Replication Lag & Read-Your-Writes Consistency | reading a replica immediately after a write returning a stale value, read-your-writes violated (verified), a fixed wait before reading the replica working under short actual lag but silently returning the previous value — not an error — under long actual lag (verified), a version-tracked read falling back to the primary until the replica has caught up, correct regardless of actual lag (verified) |
| [129](volume-23-database-internals-for-architects/paper-129-distributed-transactions-two-phase-commit/) | Distributed Transactions & Two-Phase Commit | no coordination across two services leaving a customer charged with nothing reserved after payment succeeds and inventory reservation fails (verified), a compensating refund succeeding only if the refund call itself succeeds, and failing silently — compounding the inconsistency — if it doesn't (verified), two-phase commit holding and voting in a prepare phase so a failed vote cleanly releases every hold with no partial state left behind (verified) |

---

### [Volume 24 — Event-Driven Architecture Patterns](volume-24-event-driven-architecture-patterns/)
Six real event-driven failures, each showing that a naive fix for the visible symptom (a blocked queue, a broken consumer, a duplicate charge, a race between listeners) reliably introduces or reveals a different, less visible failure that has to be checked for on its own — never assumed away by the first fix.

| Paper | Title | Examples |
|-------|-------|---------|
| [130](volume-24-event-driven-architecture-patterns/paper-130-event-ordering-partition-keys/) | Event Ordering & Partition Keys | no partition key letting a customer's own out-of-order events (update-then-delete arriving as delete-then-update) leave a permanently wrong final state (verified), routing by a single global key fixing ordering but collapsing all customers onto one partition, serializing unrelated work (verified), routing by per-customer entity key preserving guaranteed per-entity order while spreading unrelated customers across partitions for real parallelism (verified) |
| [131](volume-24-event-driven-architecture-patterns/paper-131-schema-evolution-consumer-compatibility/) | Schema Evolution & Consumer Compatibility | a field's meaning silently changing while its name and type stay the same, breaking an old consumer with no error at all (verified), a version number added to the payload that nothing actually checks, breaking the exact same way (verified), an explicit compatibility check against a known schema version correctly rejecting the incompatible payload instead of silently misinterpreting it (verified) |
| [132](volume-24-event-driven-architecture-patterns/paper-132-dead-letter-queues-poison-messages/) | Dead Letter Queues & Poison Message Handling | naive infinite retry of a poison message blocking the entire queue, 0 of 3 messages processed (verified), retry-then-silently-drop unblocking the queue but permanently losing the poison message's data with no record of it (verified), retry-then-move-to-DLQ unblocking the queue and preserving the message for inspection or replay, 2 of 3 processed plus 1 safely in the DLQ (verified) |
| [133](volume-24-event-driven-architecture-patterns/paper-133-consumer-groups-partition-rebalancing/) | Consumer Groups & Partition Rebalancing | a naive blind-accept commit letting a revoked "zombie" consumer keep processing while a new owner independently starts, double-processing 3 messages (verified), a "fix" that has the revoked consumer commit optimistically before finishing, stopping duplicates but silently skipping a message forever — permanent data loss instead of recoverable duplication (verified), epoch/generation fencing explicitly rejecting the stale consumer's commit while the real owner's succeeds, so the offset never advances past unconfirmed data (verified) |
| [134](volume-24-event-driven-architecture-patterns/paper-134-exactly-once-processing-deduplication/) | Exactly-Once Processing via Deduplication | a dedup retention window narrower than the broker's real guaranteed redelivery lag, falsely reprocessing a legitimate in-guarantee redelivery, 4 processed when 3 were expected (verified), never evicting anything correctly catching that redelivery but silently skipping a genuinely new message that later reuses the same id, 3 processed when 4 were expected — one message permanently lost (verified), a retention window matched to the broker's real guarantee getting both cases right, 4 processed exactly as expected (verified) |
| [135](volume-24-event-driven-architecture-patterns/paper-135-event-driven-choreography-implicit-coupling/) | Event-Driven Choreography & Implicit Workflow Coupling | two listeners on the same event with shipping registered first, scheduling a shipment before the fraud check even runs, with no error and no warning (verified), reordering the listeners fixing the existing pair, but a new unrelated listener added later and registered ahead of the fraud check silently reintroducing the exact same race (verified), shipping reacting only to a distinct downstream `FraudCheckPassed` event, correctly gated regardless of what order any other listener is registered in (verified) |

---

### [Volume 25 — Cloud-Native & Kubernetes Architecture](volume-25-cloud-native-kubernetes-architecture/)
Six real Kubernetes failures, each showing that a control plane guarantee that sounds absolute in its documentation ("the pod is ready," "the fleet scales to demand," "the limit contains memory," "the PDB protects the floor," "the retry recovers the client," "the same PVC comes back") is actually a narrower, more mechanical guarantee than it sounds — and the gap between the two is exactly where production incidents live.

| Paper | Title | Examples |
|-------|-------|---------|
| [136](volume-25-cloud-native-kubernetes-architecture/paper-136-liveness-vs-readiness-probes/) | Liveness vs Readiness Probes | a liveness-only probe treating "still warming up" as a crash, restarting a pod 6 times before it ever gets a chance to finish warming up, and never becoming ready (verified), a readiness probe alone routing real traffic to a not-yet-ready pod and failing 4 of 20 requests with zero restarts (verified), liveness and readiness probes used together correctly gating traffic while the pod warms up — 16 of 16 routed requests succeed, 4 correctly skipped while not ready, 0 restarts (verified) |
| [137](volume-25-cloud-native-kubernetes-architecture/paper-137-horizontal-pod-autoscaling-scaling-lag/) | Horizontal Pod Autoscaling & Scaling Lag | a conservative +1-pod-per-evaluation autoscaler dropping 70 of 100 requests in a sudden demand spike because new pods aren't ready until several ticks later (verified), a "fix" that doubles the entire fleet on every over-threshold evaluation cutting drops but wildly overshooting capacity and wasting pod-ticks in both directions (verified), a target-fleet-size calculation that scales directly to measured demand reaching the right capacity in the same number of ticks the pod-readiness delay allows, without the overshoot (verified) |
| [138](volume-25-cloud-native-kubernetes-architecture/paper-138-resource-requests-limits-oomkill/) | Resource Requests, Limits & OOMKill | no memory limit at all letting one pod's leak grow unbounded until it pushes a shared node into memory pressure, degrading a healthy neighboring pod for 27 ticks with zero visibility into the cause (verified), a memory limit alone containing the leak via repeated OOMKill-and-restart — 15 restarts, node never under pressure — but with no alert, so the same leak repeats silently forever (verified), a limit combined with a restart-count alert catching the exact same leak on the first OOMKill instead of the fifteenth (verified) |
| [139](volume-25-cloud-native-kubernetes-architecture/paper-139-rolling-updates-blue-green-canary-pdb/) | Rolling Updates, Blue/Green, Canary & PodDisruptionBudgets | a rolling deploy's own `maxUnavailable` and an unrelated node drain, each independently safe against their own floor, landing on top of each other and dropping the fleet to 5 of 10 available — 600 dropped requests — because nothing checks combined availability across both operations (verified), a PodDisruptionBudget sized far too conservatively blocking the unrelated node drain entirely for as long as no eviction headroom opens up, with no obvious symptom pointing back to the PDB as the cause (verified), a PDB sized to the real availability floor letting the drain proceed once real headroom exists, cutting the dip to a small residual timing-coincidence window instead of eliminating it outright (verified) |
| [140](volume-25-cloud-native-kubernetes-architecture/paper-140-retry-storms-service-mesh-sidecars/) | Retry Storms in Service Mesh Sidecars | naive fixed-delay retries from every client synchronizing into a repeating retry storm that a recovered downstream never gets a chance to drain, 150 total attempts across 4 storm ticks (verified, in this seeded run), exponential backoff with no jitter still synchronizing every client onto the same schedule and taking until tick 121 to drain — worse than doing nothing, because the storms just get further apart, not weaker (verified, in this seeded run), exponential backoff with full jitter desynchronizing clients onto a spread of retry times, cutting to 64 total attempts and draining by tick 15 with max simultaneous demand of 15 versus 50 (verified, in this seeded run) |
| [141](volume-25-cloud-native-kubernetes-architecture/paper-141-statefulsets-persistent-volume-claims/) | StatefulSets & Persistent Volume Claims | ephemeral, identity-less storage losing an entire cache-building effort on every reschedule, 12 replica-resets and 120 cumulative ticks of lost work over a 40-tick run (verified), a StatefulSet's default `Retain` policy correctly preserving PVC-to-ordinal identity across a scale-down-then-up cycle, but silently reattaching to now-stale data with zero validation — 2 risky reattachments, 22 staleness ticks absorbed silently, 2 orphaned PVCs left behind (verified), a StatefulSet with `whenScaled: Delete` deliberately configured for this disposable-cache workload eliminating both the staleness risk and the retained-PVC bookkeeping — 0 risky reattachments, 0 staleness ticks, 0 orphaned PVCs — a workload-specific trade-off, not a universal win (verified) |

---

### [Volume 26 — Chaos Engineering & Resilience Testing](volume-26-chaos-engineering-resilience-testing/)
Five papers on the discipline of deliberately testing whether a system's resilience actually works, rather than assuming it does — from safely causing a small amount of real damage on purpose, to knowing whether a measured result means anything, to knowing how often and how deeply that measurement needs to be repeated to keep meaning something as the system keeps changing underneath it.

| Paper | Title | Examples |
|-------|-------|---------|
| [142](volume-26-chaos-engineering-resilience-testing/paper-142-fault-injection-blast-radius/) | Fault Injection Fundamentals & Blast Radius | injecting a 100% failure system-wide with no scoping and no abort condition, failing 300 of 300 requests across the full planned duration before anyone notices (verified), scoping the same fault to a 10% canary slice cutting affected requests to 30 of 300, but still running the full planned duration with no way to stop early once harm is confirmed (verified), adding an automatic abort condition on a predeclared safety abort threshold on top of the same canary scoping, aborting after the first tick and cutting affected requests to 10 of 100 — both blast radius and duration bounded together (verified) |
| [143](volume-26-chaos-engineering-resilience-testing/paper-143-steady-state-hypothesis-experiment-design/) | Steady-State Hypothesis & Experiment Design | measuring a metric during a fault injection with no baseline to compare against, reporting a number that sounds concerning with no way to tell if it's actually different from normal (verified, in this seeded run), comparing against a baseline measured at a different, uncontrolled time, attributing a change to the fault that was actually caused by an unrelated concurrent shift in conditions (verified, in this seeded run), comparing against a contemporaneous control group measured under identical conditions at the same time, correctly attributing the measured effect to the fault and nothing else (verified, in this seeded run) |
| [144](volume-26-chaos-engineering-resilience-testing/paper-144-gamedays-vs-continuous-chaos/) | GameDays vs Continuous/Automated Chaos | a single one-time GameDay verifying a resilience mechanism once and never again, missing both of two later regressions to that same mechanism entirely (verified), a periodic GameDay on a fixed 5-tick schedule catching one regression but missing a second that starts and ends entirely between two scheduled checks (verified), continuous automated chaos checking every tick, catching both regressions the same tick they're introduced, with zero detection delay (verified) |
| [145](volume-26-chaos-engineering-resilience-testing/paper-145-dr-drills-rto-rpo-validation/) | Disaster Recovery Drills: RTO/RPO Validation | a DR plan reviewed on paper and never exercised, with a hypothetical 27-minute RTO violation and 17-minute RPO violation computed but never discovered because no drill was ever run (verified), a failover drill that actually runs but only checks the binary question "did failover complete," reporting PASS despite a 42-minute recovery time and 22-minute data loss that both blow past the stated objectives (verified), the same drill explicitly comparing those same numbers against the stated RTO/RPO, correctly reporting FAIL — RTO violated by 27 minutes, RPO violated by 17 minutes (verified) |
| [146](volume-26-chaos-engineering-resilience-testing/paper-146-resilience-regression-detection/) | Resilience Regression Detection | two services' circuit breakers never re-verified since launch, one silently removed entirely and one silently misconfigured, both undiscovered (verified), a manual code-review checklist that only checks whether the class still exists, catching the removal but passing the misconfigured one because the class is still present (verified), an automated resilience-assertion test that feeds real failures through each mechanism and asserts it actually opens by the expected threshold, catching both regressions with the same check — and throwing to fail the build when it doesn't (verified) |

---

### [Volume 27 — Modern Frontend-Backend Architecture](volume-27-modern-frontend-backend-architecture/)
Five papers tracing a single client request from server-side composition through to the client-side coordination problems that surface once a real app is built on top of it — each showing that the naive first fix for a frontend/backend correctness gap trades one visible failure mode for a quieter one, until the underlying coordination primitive is actually modeled.

| Paper | Title | Examples |
|-------|-------|---------|
| [147](volume-27-modern-frontend-backend-architecture/paper-147-backend-for-frontend-pattern/) | The Backend-for-Frontend Pattern | a single shared backend response format forcing every client to receive and parse fields it doesn't need, unable to independently degrade one section without breaking every other client (verified), a client-specific endpoint duplicating aggregation logic per client with no shared failure-isolation boundary (verified), a BFF layer composing per-client responses from shared backend services with independently degradable sections (verified) |
| [148](volume-27-modern-frontend-backend-architecture/paper-148-ssr-csr-hydration-consistency/) | SSR/CSR Hydration Consistency | trusting a server-rendered snapshot at hydration time with no staleness check, silently rendering stale data as if it were current (verified), a naive re-fetch-everything-at-hydration fix eliminating staleness but discarding the SSR performance win entirely (verified), a snapshot-age-aware hydration that reuses the SSR snapshot when still fresh and re-fetches only when stale (verified) |
| [149](volume-27-modern-frontend-backend-architecture/paper-149-optimistic-ui-conflict-resolution/) | Optimistic UI Updates & Conflict Resolution | an optimistic update with no ordering guard, a late-arriving stale server response silently overwriting a newer optimistic state (verified), a naive "last response wins" fix still applying responses in arrival order rather than request order, still capable of regressing state (verified), a request-sequence-aware reconciliation that only applies a response if it's newer than the last-applied sequence, correctly discarding the stale one (verified) |
| [150](volume-27-modern-frontend-backend-architecture/paper-150-stale-while-revalidate-caching/) | Stale-While-Revalidate Caching | a cache that never revalidates, serving a permanently stale price after the real value changes server-side with no mechanism to notice (verified), synchronous revalidation on every read blocking every read and discarding a good cached value on a transient revalidation failure (verified), stale-while-revalidate serving the cached value first, revalidating after, and keeping the last known-good value flagged possibly-stale on failure instead of discarding it (verified) |
| [151](volume-27-modern-frontend-backend-architecture/paper-151-concurrent-token-refresh-races/) | Concurrent Token Refresh Races | three concurrent calls each independently refreshing a rotating, single-use refresh token, only the first succeeding and the other two rejected for reusing an already-rotated token (verified), a naive in-progress guard flag stopping the duplicate refresh calls but causing the two waiting callers to give up immediately instead of learning the in-flight refresh later succeeded (verified), a singleflight pattern where callers join the one pending refresh instead of racing or abandoning it, letting all three succeed off a single refresh call (verified) |

---

### [Volume 28 — Data Privacy & Compliance Architecture](volume-28-data-privacy-compliance-architecture/)
Five papers on the structural side of privacy compliance — where "we have a policy for this" isn't enough, because the policy has to be enforced by code that can't be talked around, bypassed by an innocent mistake, or quietly left unverified. Each paper builds a real, working default-deny or tamper-evident mechanism, then honestly shows its remaining limits rather than claiming the toy example is a complete production system.

| Paper | Title | Examples |
|-------|-------|---------|
| [152](volume-28-data-privacy-compliance-architecture/paper-152-data-retention-right-to-erasure/) | Data Retention & Right to Erasure | an erasure request marked complete after a single delete call with no verification of which downstream stores actually received it, leaving one store untouched and undetected (verified), a naive fix that checks each store's response but stops at the first failure, leaving later stores unattempted and unreported (verified), an erasure manifest that attempts every target store regardless of earlier failures and reports exactly which targets succeeded and which didn't, so an incomplete erasure is never silently reported complete (verified) |
| [153](volume-28-data-privacy-compliance-architecture/paper-153-pii-tokenization-masking/) | PII Tokenization & Masking | a support tool that logs and displays a customer's real SSN on every lookup, with no distinction between an operation that needs the real value and one that doesn't (verified), a naive masking fix that masks the display but still logs and holds the real value in memory for every lookup regardless of purpose (verified), a tokenization layer that stores only a token by default and reveals the real value solely through a scoped, logged reveal call for an operation that actually needs it (verified) |
| [154](volume-28-data-privacy-compliance-architecture/paper-154-consent-management-purpose-limitation/) | Consent Management & Purpose Limitation | a marketing email sent based only on whether a customer has ever granted any consent, ignoring which purpose that consent was actually for (verified), a naive purpose check that reads the most recently recorded consent record but not whether it was later revoked (verified), a purpose-scoped consent ledger that evaluates the current, non-revoked consent for the specific purpose in question before allowing the action (verified) |
| [155](volume-28-data-privacy-compliance-architecture/paper-155-data-residency-cross-border-transfer/) | Data Residency & Cross-Border Transfer | a data-access path with no residency check at all, serving a cross-border read with no record that a boundary was even considered (verified), a naive residency gate that treats any non-null transfer-approval reference — including a blank string — as a valid approval, silently allowing an unapproved transfer through (verified), a default-deny residency gate that requires a same-zone request or a non-blank approval reference on file, correctly denying an unapproved cross-border request across six distinct request shapes (verified) |
| [156](volume-28-data-privacy-compliance-architecture/paper-156-immutable-audit-logging-compliance/) | Immutable Audit Logging for Compliance | an ordinary mutable audit table where a row documenting unauthorized access is deleted outright, leaving no trace the access, or the deletion, ever happened (verified), a per-entry self-computed hash that looks tamper-evident but is defeated by editing an entry's content and recomputing that same entry's own hash, reporting VALID before and after the tampering (verified), a hash-chained, append-only log that catches an interior edit or deletion by breaking the chain, but — shown honestly rather than just claimed — still misses a deleted tail entry or a fully recomputed suffix rewrite unless separately checked against an externally anchored checkpoint, which closes that exact gap (verified) |

---

### [Volume 29 — Cost-Aware Architecture / FinOps for Engineers](volume-29-cost-aware-architecture-finops/)
Five papers on cost-aware engineering — where a policy tuned for the average pod, workload, object, job, or query category quietly overcharges or undercharges every instance that isn't average. Each paper shows a uniform or proxy-based cost policy failing a specific, verified entity, then builds a per-entity historical-cost-projection policy with an honest, disclosed fallback for the entity with too little history to project from.

| Paper | Title | Examples |
|-------|-------|---------|
| [157](volume-29-cost-aware-architecture-finops/paper-157-cost-attribution-chargeback/) | Cost Attribution & Chargeback | a shared Kubernetes node's cost split evenly by pod count regardless of actual usage, billing a CPU-heavy pod 25% of a bill it drove 85.7% of (verified), a naive per-second usage-proportional split that reconciles to the exact bill but ignores which workloads are billable versus platform-shared overhead (verified), a usage-weighted allocation that separates shared platform overhead from team-attributable usage before splitting, disclosed and reconciled to the same total bill (verified) |
| [158](volume-29-cost-aware-architecture-finops/paper-158-right-sizing-autoscaling-cost-traps/) | Right-Sizing & Autoscaling Cost Traps | a rightsizing recommendation computed from a workload's own real 30-day usage average, resizing a request down to a level its own history already showed being exceeded twice by flash-sale spikes (verified), a naive percentile-based fix still using a single blanket percentile untuned to a specific workload's own spike frequency (verified), a workload-specific candidate-percentile evaluation that projects cost and risk per workload from its own history, including a disclosed minimum-sample fallback (verified) |
| [159](volume-29-cost-aware-architecture-finops/paper-159-storage-tiering-data-lifecycle-costs/) | Storage Tiering & Data Lifecycle Costs | an age-based archive-tier policy archiving a compliance object retrieved twice monthly, where cheap archive storage plus per-retrieval fees costs more than leaving it in standard tier (verified), a naive recency-based fix confusing an object's single most recent access with its own steady-state retrieval frequency, misclassifying in both directions (verified), a per-object candidate-tier cost projection from each object's own historical retrieval frequency, with a disclosed minimum-history fallback (verified) |
| [160](volume-29-cost-aware-architecture-finops/paper-160-spot-preemptible-compute-cost-aware-degradation/) | Spot/Preemptible Compute & Cost-Aware Degradation | a batch job moved to spot with no checkpointing, whose own 6-interruption restart-from-scratch history makes spot cost more than on-demand for that one job (verified), a naive blanket 15-minute checkpoint interval that fixes that job but adds real overhead to a job whose own history shows it's almost never interrupted (verified), a per-job candidate-checkpoint-interval cost projection — including "no checkpointing" as a real option — checked against that job's own on-demand cost, with a disclosed minimum-run fallback (verified) |
| [161](volume-29-cost-aware-architecture-finops/paper-161-ai-llm-inference-cost-routing/) | AI/LLM Inference Cost-Routing | every query routed to one large language model regardless of category, overpaying for a category whose own 98%-historical-accuracy small-model track record projects a far cheaper cascade route (verified), a naive word-count-threshold proxy misrouting in both directions, landing a $4.10 modeled total worse than the uniform baseline's $4.00 (verified), a per-category candidate-route cost projection from each category's own historical small-model accuracy, with a disclosed minimum-sample cutoff for a brand-new category (verified) |

---

### [Volume 30 — Capstone: One System, End to End](volume-30-capstone-one-system-end-to-end/)
The final volume of the entire 168-paper plan. Unlike every prior volume's 3 unrelated toy problems per paper, all 7 papers here trace one single fictional system — "Nova," a multi-tenant B2B order/checkout and fulfillment platform — through a different architectural concern each paper, each explicitly tying its mechanism back to a named earlier volume rather than inventing a new domain from scratch. The closing paper ties every earlier mechanism into one unified cost-and-correlation model, closing the loop on the whole series.

| Paper | Title | Examples |
|-------|-------|---------|
| [162](volume-30-capstone-one-system-end-to-end/paper-162-nova-requirements-architecture-tradeoffs/) | Nova's Requirements → Architecture Trade-off Table | a single-column "recommended architecture" chosen with no stated trade-off against Nova's actual constraints (verified), a trade-off table scored against a generic checklist rather than Nova's own disclosed constraints, silently recommending the wrong option (verified), a trade-off table scored against Nova's own disclosed constraints correctly ranking the option actually built for them, establishing the architecture every later paper in this volume builds on (verified) |
| [163](volume-30-capstone-one-system-end-to-end/paper-163-nova-api-boundary-multitenant/) | Nova's API Boundary Across Tenants | a shared endpoint silently repurposing an existing field's meaning with no version signal, breaking an unchanged billing client with no error (verified), naive per-tenant endpoint duplication whose maintenance surface grows 1:1 with tenant count, reaching 50 at Nova's own disclosed minimum (verified), additive-only field evolution serving every tenant from one shared endpoint with no per-tenant maintenance cost, qualified by an explicit tolerant-reader precondition (verified) |
| [164](volume-30-capstone-one-system-end-to-end/paper-164-nova-order-fulfillment-data-path/) | Nova's Order-to-Fulfillment Data Path | a direct, uncoordinated write across the order and inventory stores leaving a permanent inconsistency on partial failure (verified), an outbox-pattern fix without idempotent consumption closing the delivery gap but double-applying a redelivered event (verified), an outbox + idempotent-consumer pair, committed in one shared atomic local transaction, closing both gaps together (verified) |
| [165](volume-30-capstone-one-system-end-to-end/paper-165-nova-read-path-multitenant-load/) | Nova's Read Path Under Multi-Tenant Load | a shared cache with no tenant isolation letting one tenant's cold scan evict another tenant's hot working set (verified), per-tenant cache partitioning that closes cross-tenant eviction but a naive fixed even-split doesn't fit a legitimately larger tenant's own historical working set (verified), usage-weighted partition sizing giving each tenant a partition matching its own historical need (verified) |
| [166](volume-30-capstone-one-system-end-to-end/paper-166-nova-resilience-verification/) | Verifying Nova's Resilience Actually Holds | a circuit breaker checked once before a refactor and then treated as permanently settled (verified), a one-time manual GameDay chaos drill whose recorded result goes stale the moment a later refactor changes the call path (verified), a continuous automated chaos suite re-executing the identical failure-injection experiment against whichever call path each deploy actually selects (verified) |
| [167](volume-30-capstone-one-system-end-to-end/paper-167-nova-security-tenant-compliance/) | Masking What's Shown Is Not the Same as Guarding What's Logged | a support tool logging a customer's real PII on every lookup with no purpose check and no residency check (verified), a masking fix that redacts the agent's own screen while leaving the underlying audit-log call completely unchanged (verified), tokenization composed with a purpose gate and a residency gate closing all three gaps together around one lookup path (verified) |
| [168](volume-30-capstone-one-system-end-to-end/paper-168-nova-cost-aware-operation-capstone-synthesis/) | A Cost Total and a Separate Health Log Without a Shared Key Leave Cost-to-Mechanism Pairing Fragile | Nova's operational cost tracked as a single running total with no attribution to any tenant or mechanism (verified), two independent logs — cost and mechanism/health — that stay aligned only because of this program's fixed call order, not because either log's own entries record the pairing (verified), a unified `OperationEvent` co-locating tenant, mechanism, health status, and cost delta as fields on one object, correctly attributing the largest cost contributor directly from the recorded fields (verified) — the final paper of the entire 168-paper plan |

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
