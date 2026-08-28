# URL Shortener — Evaluated with Quality Attribute Scenarios

**Evaluation method:** Quality Attribute Scenarios (QAS) — Bass, Clements & Kazman, *Software Architecture in Practice*

**Opens Volume 10** — 7 system design blueprints, each paired with one formal architecture-evaluation method.

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

The classic warm-up interview question, evaluated the way a principal architect actually would: against a Quality Attribute Scenario that forces specific, measurable claims instead of "fast and reliable." Random-code-with-retry looks correct in every demo and degrades predictably as the keyspace fills (measured). Centralizing a monotonic counter fixes collisions but makes every request pay a network round trip (measured: ~90x slower than block allocation). Skipping collision checking entirely because "the ID space is basically infinite" is a birthday-paradox trap (measured: real collisions at ~1,283 requests against a 1,048,576-value space, not at 1,048,576).

## The QAS

| Part | This scenario |
|------|----------------|
| Source | 5 application servers, concurrent shorten requests |
| Stimulus | A burst of 2,500 concurrent requests |
| Artifact | The ID-generation component |
| Environment | Normal operation |
| Response | Every request gets a code; no code issued twice |
| Response Measure | p99 latency under load; zero collisions, verified exhaustively |

## The Fix

**Block allocation**: each node fetches a block of IDs (1,000 at a time) from a central counter in one round trip, then hands them out locally with zero network cost until the block runs out. Measured: 14ms vs. 1,280ms for the same 2,500 codes across 5 nodes — same collision-free guarantee, ~90x faster.

## Code Examples in This Repo

| Example | What It Shows |
|---------|--------------|
| [`pressure/`](./pressure/) | Random 3-char code + collision retry — measured attempts climbing from 1.0 to 19.5 as the keyspace fills from 0% to 95% |
| [`principle-applied/`](./principle-applied/) | Centralized counter (measured 1,280ms/2,500 codes) vs. block-allocated counter (measured 14ms/2,500 codes) — both zero collisions |
| [`wrong-direction/`](./wrong-direction/) | Random ID with no collision check — measured 3 real collisions in 2,000 requests against a "basically infinite" 1,048,576-value space, matching the birthday-paradox prediction |

### How to Run

```bash
cd volume-10-system-design-blueprints/paper-52-url-shortener/pressure
javac *.java && java Main

cd volume-10-system-design-blueprints/paper-52-url-shortener/principle-applied
javac *.java && java Main

cd volume-10-system-design-blueprints/paper-52-url-shortener/wrong-direction
javac *.java && java Main
```
