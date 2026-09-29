# Cache Eviction — LFU Fixes Scans, but Frequency Never Forgets

**Pattern:** Pure LRU (recency-only eviction — a recurring scan of one-time keys evicts a genuinely hot working set every single cycle) → pure LFU (frequency-only eviction with admission control — genuinely resists the scan, but frequency counts that never decay lock the cache to whatever was popular first, even after the real workload shifts) → LFU with periodic decay (halves every resident's frequency count on a schedule, preserving scan resistance while letting stale popularity fade)

**Volume 22** (Caching Architecture Deep Dive) — Paper 118.

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

LRU and LFU eviction policies fail in opposite directions: LRU evicts purely by recency, so a recurring one-time bulk-access pattern (a backup job, a scan, a bulk export) evicts a genuinely hot working set every time it runs, forcing avoidable backend re-fetches (verified: 15 avoidable misses across 5 recurring scan cycles against a 3-key hot set). Switching to pure LFU — evicting by frequency, with admission control that rejects any candidate whose starting frequency can't beat every resident — genuinely fixes that failure (verified: 147/150 hit rate held through the same 5 scan cycles), but frequency counts that never decay create a different, opposite failure: once a workload shifts and a new set of keys becomes the real hot set, their fresh counts can never catch up to the old set's permanently high counts, so the cache never adapts (verified: 0/150 hit rate on a genuinely new working set, indefinitely). The real fix keeps LFU's admission logic unchanged and adds one operation — periodically halving every resident's frequency count — which preserves the exact same scan resistance while letting stale popularity actually fade (verified: 147/150 scan resistance held, and the new working set reached 117/150, with the first successful admission at round 12 of the shift instead of never).

## The Pressure: A Recurring Scan Evicts the Hot Set Every Single Time

A small, genuinely hot working set holds a strong hit rate entirely on its own. But under pure LRU eviction, a recurring one-time bulk-access job — touching many other keys, each exactly once, never again — evicts the entire hot set every time it runs, because recency alone can't distinguish "touched once, never again" from "touched constantly." The hot set never actually goes cold; every resulting miss is avoidable.

## The Principle

**An eviction policy's fix has to be verified against more than the failure that motivated it.** LFU's scan resistance is real and measurable, but verifying only the scan scenario would have missed the failure it introduces: a frequency count that only ever increases is a record of historical popularity, not a signal of current relevance, and those diverge the moment a workload genuinely shifts. The two properties — resisting one-time bulk access and adapting to a changing workload — aren't mutually exclusive; periodic decay buys both, at the cost of one small, cheap, periodic bookkeeping operation.

## Pros

- Periodic decay preserves the exact scan-resistance numbers LFU already achieved — nothing is traded away to gain adaptability
- A single, simple mechanism (halve all counts on a schedule) fixes the historical-popularity-lock-in failure without introducing a second, separate data structure
- The decay period is a single tunable knob, independently adjustable against how bursty the scan traffic is versus how quickly the real workload is expected to shift

## Cons

- Choosing a decay period requires knowing (or estimating) both the scan/burst cadence and the expected workload-shift cadence — too frequent a decay reintroduces vulnerability to scans; too infrequent a decay reintroduces the lock-in failure
- Adds one additional piece of state (a tick counter, or a background timer) to an otherwise stateless-per-access frequency map
- Doesn't eliminate the brief re-adaptation lag after a genuine workload shift — the cache is right to hold onto the old set for a little while, since that's exactly what protects it from being fooled by noise, but this means adaptation isn't instantaneous

## When Plain LRU Is Still the Right Choice

- The workload has no recurring one-time bulk-access pattern that would evict a real hot set
- Recency actually is the best predictor of future access for the workload in question (e.g., recently-viewed items, session data)
- The cost of the occasional scan-induced miss is genuinely negligible, and the added complexity of frequency tracking and decay tuning isn't worth it

## Code Examples in This Repo

| Example | Domain | What It Shows |
|---------|--------|--------------|
| [`pressure`](./pressure) | Pure LRU | A recurring scan evicts a real hot set every cycle; 15 avoidable misses across 5 cycles |
| [`wrong-direction`](./wrong-direction) | Pure LFU, no decay | Scan resistance genuinely fixed (147/150), but a workload shift is never adopted (0/150, permanently) |
| [`principle-applied`](./principle-applied) | LFU with periodic decay | Same scan resistance (147/150) plus real adaptation (117/150), starting at round 12 of the shift |

### How to Run

```bash
cd volume-22-caching-architecture-deep-dive/paper-118-cache-eviction-lru-vs-lfu/pressure
javac *.java && java Main

cd volume-22-caching-architecture-deep-dive/paper-118-cache-eviction-lru-vs-lfu/wrong-direction
javac *.java && java Main

cd volume-22-caching-architecture-deep-dive/paper-118-cache-eviction-lru-vs-lfu/principle-applied
javac *.java && java Main
```
