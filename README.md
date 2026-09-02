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
