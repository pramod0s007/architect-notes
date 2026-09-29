# Write-Ahead Log & Crash Recovery — A Log Isn't Durable Until It's Acknowledged After the Fsync

**Pattern:** No durability mechanism at all (50 writes acknowledged, 0 recovered after a crash) → a write-ahead log that buffers entries and acknowledges before the flush (47 writes acknowledged, only 40 recovered — 7 lost) → a write-ahead log that acknowledges only after the entry is confirmed durable (same 47 writes, 47 recovered, 0 lost)

**Volume 23** (Database Internals for Architects) — Paper 124.

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

A write-ahead log's job is to make a write recoverable after a crash by recording the intent to make that change before it's applied anywhere else — but a log entry sitting in an unflushed in-memory buffer is exactly as vulnerable to a crash as having no log at all (verified: with no durability mechanism whatsoever, 50 acknowledged writes drop to 0 recovered after a simulated crash). Adding a write-ahead log genuinely helps, but only up to the point where the acknowledgment happens: a WAL that buffers entries and flushes them in batches, while acknowledging a write the moment it's buffered rather than once it's flushed, still loses every write caught in an unflushed buffer at crash time (verified: 47 acknowledged, only 40 recovered, 7 lost). The real fix isn't a different logging strategy — it's moving the acknowledgment point to after the log entry is confirmed durable, closing the gap completely (verified: same 47 writes, same crash point, 47 recovered, 0 lost).

## The Pressure: No Durability Mechanism, a Crash Loses Everything

A database that holds its data only in an in-memory map has nothing that survives a process crash — every acknowledged write only ever existed in volatile memory, and a crash doesn't lose "some" of that data, it loses all of it, unconditionally, regardless of how many writes were acknowledged or how long ago.

## The Principle

**A write-ahead log's existence and its durability at any given instant are two separate facts, and the only correct moment to acknowledge a write is after the log entry backing it is confirmed durable — never before.** A WAL that buffers entries in memory and flushes them periodically is a real, substantial improvement over having no log at all, but if the acknowledgment happens on buffer-append instead of on-flush, it silently reintroduces the exact failure the WAL exists to prevent, just for a smaller, less obvious set of writes. Batching flushes together for throughput (group commit) is fine and doesn't conflict with correctness — as long as the acknowledgment is deferred along with the flush, not issued the moment the entry lands in the buffer.

## Pros

- Fsync-before-acknowledge closes the durability gap completely — verified zero writes lost across a crash, compared to 7 lost with buffer-before-acknowledge and 50 lost with no log at all
- Recovery is a straightforward, deterministic replay of durable log entries into a fresh in-memory structure — no partial or ambiguous state to reconcile
- Compatible with batching fsyncs for throughput (group commit), as long as the acknowledgment waits for the batched flush rather than the buffer-append

## Cons

- Fsync-before-acknowledge on every write is markedly slower than buffering, because every write now waits on a real disk flush before it can return
- Doesn't by itself address read-side durability failures (e.g. a query returning stale or reordered results) — this pattern only covers write acknowledgment matching actual durability
- Group commit reintroduces the same class of risk if implemented carelessly — the win only holds if every acknowledgment is tied to the batch's flush, not to individual buffering

## When a Buffered (Periodically Flushed) WAL Is Still Acceptable

- The acknowledgment itself is explicitly deferred until after the batch flush, not issued the moment the entry is buffered — this is exactly what correctly implemented group commit does: batch fsyncs for throughput, but hold every acknowledgment until that batched fsync actually completes
- The workload can tolerate losing a bounded, disclosed window of the most recent writes on crash (e.g. best-effort metrics or analytics ingestion), and that trade-off is an explicit agreement with the caller, not an assumption hidden inside the acknowledgment
- A separate, explicit "durable" acknowledgment exists alongside a faster "accepted" one, and callers that need the durability guarantee wait for the correct one

## Code Examples in This Repo

| Example | Domain | What It Shows |
|---------|--------|--------------|
| [`pressure`](./pressure) | No durability mechanism | 50 writes acknowledged, 0 rows recovered after a simulated crash |
| [`wrong-direction`](./wrong-direction) | WAL, buffered, ack before flush | 47 writes acknowledged, only 40 recovered — 7 lost from the unflushed buffer |
| [`principle-applied`](./principle-applied) | WAL, ack only after fsync | Same 47 writes, 47 recovered, 0 lost |

### How to Run

```bash
cd volume-23-database-internals-for-architects/paper-124-write-ahead-log-crash-recovery/pressure
javac *.java && java Main

cd volume-23-database-internals-for-architects/paper-124-write-ahead-log-crash-recovery/wrong-direction
javac *.java && java Main

cd volume-23-database-internals-for-architects/paper-124-write-ahead-log-crash-recovery/principle-applied
javac *.java && java Main
```
