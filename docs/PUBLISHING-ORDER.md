# Publishing Order — Architect Notes Series

> Strategy: publish by virality potential, not paper number. Hook readers early with controversy (Paper 13), depth (Paper 01), and utility (Paper 14). Fill the series in after the audience is built.

---

## Phase 1 — Build the Audience (Weeks 1–4)

These four papers establish the series identity and attract the widest audience.

| Week | Paper | Title | Why First |
|------|-------|-------|-----------|
| 1 | **01** | Why Memorizing Design Patterns Is Holding You Back | Foundation. Sets the "pressure-first" thesis that all other papers build on. Strong hook. |
| 2 | **13** | When Patterns Become Anti-Patterns | Most shareable. Engineers forward this to their teams. Controversy + self-recognition. |
| 3 | **04** | Strategy Pattern Through Real Refactoring | Most-searched pattern. High SEO. Good entry for engineers who find via Google. |
| 4 | **14** | Pattern Selection Decision Tree | Highest practical utility. Engineers bookmark this. High return visits. |

---

## Phase 2 — Establish Framework (Weeks 5–8)

Fill in the conceptual foundation for readers who want the complete picture.

| Week | Paper | Title | Why |
|------|-------|-------|-----|
| 5 | **02** | The Four Architectural Buckets | The classification framework. Readers who liked Paper 01 need this. |
| 6 | **15** | Which Patterns Still Matter in 2026 | Timely angle + AI hook. Second sharing peak. |
| 7 | **03** | The Death of if-else | High-search topic. Reaches developers who Google "when to replace if-else". |
| 8 | **06** | Command Pattern Through Banking Systems | CQRS angle = relevant to modern engineers. Banking = universal domain. |

---

## Phase 3 — Pattern Deep Dives (Weeks 9–14)

Individual pattern papers for readers who want depth on specific patterns.

| Week | Paper | Title | Why |
|------|-------|-------|-----|
| 9  | **05** | State Pattern Through a StopWatch | Order workflows + state machines = relevant to most backend engineers. |
| 10 | **09** | Specification Pattern | Enterprise relevance. Less-known pattern = differentiated content. |
| 11 | **10** | Chain of Responsibility | Every engineer has built middleware. High recognition, new framing. |
| 12 | **07** | Visitor Pattern Without UML | Niche but deep. Reaches AST/compiler engineers. "Without UML" hook. |
| 13 | **08** | Lookup Tables vs Polymorphism | Architectural decision-making. Engineers who want trade-off thinking. |
| 14 | **11** | Builder Pattern + **12** Factory Pattern | Creational pair. Most engineers use both — clear naming wins readers. |

---

## Phase 4 — Complete the Series (Volume 5, Weeks 15–22)

The 8 missing GoF patterns. Each is a standalone paper following the same format.

| Week | Paper | Pattern | Priority |
|------|-------|---------|----------|
| 15 | **16** | Observer | Critical — foundation of event-driven, RxJava, Kafka, reactive |
| 16 | **17** | Decorator | Critical — Java I/O, Spring AOP, middleware — engineers see this daily |
| 17 | **18** | Proxy | High — lazy loading, caching, gRPC stubs, security proxies |
| 18 | **19** | Adapter | High — legacy integration, third-party API wrapping |
| 19 | **20** | Facade | High — service layer, SDK design, every microservice exposes one |
| 20 | **21** | Template Method | Medium — Spring JdbcTemplate, abstract workflow steps |
| 21 | **22** | Singleton | Medium — teach as cautionary tale + correct enum pattern |
| 22 | **23** | Iterator | Lower — custom traversal, cursor patterns, less novel |

---

## Medium Publication Tips

### Tags to use on every paper
`software-engineering` `design-patterns` `software-architecture` `java` `system-design`

### Publications to submit to
- Better Programming (largest audience for technical content)
- Level Up Coding
- The Pragmatic Programmer
- JavaScript in Plain English (if you add JS examples later)

### Cross-linking strategy
- End every paper with a "← Previous | → Next" navigation footer
- Reference related papers within the body (e.g., "See Paper 13 for when this pattern becomes harmful")
- Keep a pinned Medium list: "Architect Notes — Complete Series"

### Posting cadence
- One paper per week, Tuesday or Wednesday (highest Medium engagement days)
- Share on LinkedIn immediately after publishing (tag: #softwareengineering #designpatterns #systemdesign)
- GitHub repo link in every paper footer drives stars

---

## GitHub Release Tags

Tag the repo at each phase completion:

```bash
# After Phase 1 (4 papers)
git tag v1.0.0-phase1

# After Phase 2 (8 papers)
git tag v1.1.0-phase2

# After Phase 3 (14 papers)
git tag v2.0.0-complete

# After Phase 4 (all 23 patterns)
git tag v3.0.0-all-23-patterns
```
