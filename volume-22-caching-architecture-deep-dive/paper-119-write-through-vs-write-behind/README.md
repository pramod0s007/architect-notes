# Write-Through vs Write-Behind — Durable Isn't the Same as Fast Enough

**Pattern:** Write-behind, no durable log (fast — no backend round trip per write — but a crash before the scheduled flush loses everything buffered, not delays it) → write-through (every write durable synchronously, genuinely fixing data loss, but every write now pays a full backend round trip, blowing through the system's own latency budget under a burst) → write-behind backed by a durable write-ahead log (each write durable via a cheap local log append, backend flush still batched and deferred — gets write-behind's speed and write-through's durability guarantee at once)

**Volume 22** (Caching Architecture Deep Dive) — Paper 119.

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

Write-behind and write-through are usually framed as a simple speed-versus-durability tradeoff, but that framing hides a real fix: write-behind accepts writes fast by buffering them in memory and deferring the backend write to a scheduled flush, but a crash before that flush runs loses every buffered write entirely, not partially (verified: 30 writes acknowledged, 0 durable after a crash). Switching to write-through genuinely fixes the data loss — every write is durable the instant it's accepted, with zero writes lost under any crash timing (verified: 30/30 durable) — but it introduces a separate, equally real cost: every write now pays a full backend round trip, and under a burst of writes that cost compounds past the system's actual latency budget (verified: 150 ticks spent against a 100-tick budget, only 20 of 30 writes completing in time). The real fix decouples "durable" from "in the backend": a write-ahead log makes each write durable via a cheap, sequential, local append, while the backend flush stays batched and deferred exactly like write-behind (verified: 30 ticks for the same burst, well within budget, and all 30 writes recovered by replaying the log after a simulated crash).

## The Pressure: Acknowledged Doesn't Mean Durable

A write-behind cache is fast because it defers the expensive part — the backend write — to a scheduled flush, and acknowledges the caller before that flush ever runs. That's not a bug; it's the entire mechanism. But it means "the write succeeded" and "the write is durable" are two different events, separated by however long it takes the next flush to run, and a crash inside that window doesn't delay the write, it erases it.

## The Principle

**A fix verified against one failure mode isn't verified against a different one.** Write-through's durability claim — zero writes lost, ever — is true and complete. Its throughput claim was never checked, because the team that made the switch was solving the crash-loss incident, not auditing the system's latency budget. The two properties (durability, throughput) are genuinely separable: a durable write-ahead log gets both, because it makes "durable" cheap (a local, sequential append) without requiring "durable" to mean "already in the backend."

## Pros

- A write-ahead log's per-write cost is a fraction of a backend round trip (a local sequential append vs. a network call), so it scales to a much higher write volume before hitting a latency budget
- Recovery is deterministic: replaying the log after a crash restores every write that was ever accepted, with no partial or ambiguous state
- The backend flush logic is unchanged from plain write-behind — the log is additive, not a rearchitecture of the batching mechanism

## Cons

- Requires the log itself to be genuinely durable (fsync'd or otherwise crash-safe) — a log that only exists in the same process memory as the buffered writes provides no protection at all
- Adds a recovery/replay code path that must be exercised and tested, not just assumed to work, since it only runs during the exact failure scenario it exists to handle
- The log needs its own lifecycle (truncation after a successful flush, growth bounds) that a pure write-behind or write-through design doesn't need to think about

## When Plain Write-Through Is Still the Right Choice

- Write volume is low enough that the per-write backend round-trip cost never approaches the system's latency budget
- The system has no meaningful "batch" boundary to defer work to, so write-through's simplicity (no log, no replay logic, no flush scheduling) is a real advantage
- The backend itself is fast enough that a round trip is cheap relative to everything else the request is already doing

## Code Examples in This Repo

| Example | Domain | What It Shows |
|---------|--------|--------------|
| [`pressure`](./pressure) | Write-behind, no durable log | 30 writes acknowledged, 0 durable after a crash before the scheduled flush |
| [`wrong-direction`](./wrong-direction) | Write-through | 30/30 durable (0 lost), but 150 ticks spent against a 100-tick budget |
| [`principle-applied`](./principle-applied) | Write-behind + write-ahead log | 30 ticks spent (within budget), 30/30 recovered by replaying the log after a crash |

### How to Run

```bash
cd volume-22-caching-architecture-deep-dive/paper-119-write-through-vs-write-behind/pressure
javac *.java && java Main

cd volume-22-caching-architecture-deep-dive/paper-119-write-through-vs-write-behind/wrong-direction
javac *.java && java Main

cd volume-22-caching-architecture-deep-dive/paper-119-write-through-vs-write-behind/principle-applied
javac *.java && java Main
```
