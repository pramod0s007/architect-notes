# Local-Prod Parity — The Bug Was Never in the Code, It Was in the Difference

**Continues Volume 19** — picking up right after Paper 102 (CI Flakiness and the Retry Trap).

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

A local development substitute that behaves differently from production on even one specific property can pass identical code that production would reject, with no way for the developer to see the discrepancy (measured: the same code and inputs succeeded locally and would have failed against production's real collation). Matching the one specific behavior that actually determines correctness lets local development catch the bug immediately, without requiring a full replica of production's scale or infrastructure (measured: the identical test correctly failed locally once the store matched production's real comparison behavior). This paper then shows the overcorrection: a fully faithful parity environment too expensive for routine use, bypassed by a faster, unfaithful fallback for daily work (measured: the correct store would have caught the bug, but the store developers actually iterate against daily still had it).

## The Measurements

- **Unfaithful lightweight store (pressure)**: identical code creating "Alice" then "alice" succeeds locally (2 accounts) but would be rejected against production's real case-insensitive collation (1 account).
- **Targeted parity (principle-applied)**: local dev now matches production's collation exactly — the duplicate is correctly rejected during local development, before deploy.
- **Full-scale parity, too expensive (wrong-direction)**: a correctly-behaving parity store takes ~300-330ms to provision 2 million rows, too slow for routine use — daily development falls back to the original unfaithful store, and the bug resurfaces.

## The Finding

**Parity has to be measured by what a developer's actual, most-frequently-used workflow exercises, not by what technically exists somewhere in the pipeline.** An unused correct environment provides zero real protection — the goal is matching the specific behaviors that determine correctness, fast enough to use on every save.

## Code Examples in This Repo

| Example | What It Shows |
|---------|--------------|
| [`pressure/`](./pressure/) | Case-sensitive local store — measured identical code diverging between local and production-like behavior |
| [`principle-applied/`](./principle-applied/) | Targeted collation parity — measured the bug caught locally, before deploy |
| [`wrong-direction/`](./wrong-direction/) | Full-scale parity, too slow for daily use — measured the bug resurfacing via a bypassed fallback store |

### How to Run

```bash
cd volume-19-platform-engineering-developer-experience/paper-103-local-prod-parity/pressure
javac *.java && java Main

cd volume-19-platform-engineering-developer-experience/paper-103-local-prod-parity/principle-applied
javac *.java && java Main

cd volume-19-platform-engineering-developer-experience/paper-103-local-prod-parity/wrong-direction
javac *.java && java Main
```
