# MVCC & Snapshot Isolation — Correctness and Concurrency Don't Have to Compete

**Pattern:** No transaction isolation (a reader combines a pre-transfer and post-transfer balance, computing a total — 250 — that never existed) → a global lock held for the whole read transaction (correct, total is 200, but the writer is blocked for the reader's entire ~210ms transaction window) → MVCC with snapshot isolation (same correct total, writer completes in 0ms with no lock contention at all)

**Volume 23** (Database Internals for Architects) — Paper 125.

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

Per-operation atomicity — each individual read or write completing correctly — is not the same guarantee as transaction-level isolation, which covers what a multi-step transaction is allowed to see across its entire duration. A store where every single read and write is individually atomic can still let a report transaction observe read skew: reading one account's balance before a concurrent transfer and the other account's balance after it, producing a total that never existed at any single instant (verified: a reader computes 250 when the true, conserved total is 200). The obvious fix — holding a lock for the whole read transaction — does restore correctness, but at a real, measured cost: a concurrent writer touching entirely different data still has to wait for the reader's lock to be released (verified: a writer blocked for ~210ms, matching the reader's ~200ms transaction). The actual fix replaces overwrites with versions: every write commits a new, timestamped version, and a transaction is pinned to a snapshot timestamp captured once at its start, so it consistently ignores anything committed afterward — without ever taking a lock that a writer has to wait on (verified: same correct total, writer completes in 0ms).

## The Pressure: No Isolation Lets a Reader See Half a Transfer

A transaction that reads several related values one at a time has no guarantee that all of those reads reflect a single, consistent point in time — even if every individual read is itself perfectly atomic. If a conflicting write commits in the gap between two reads that are supposed to be part of the same logical view, the reader ends up combining values from two different moments, and the combination can be an answer that was never actually true at any point.

## The Principle

**Isolation is a guarantee about what an entire transaction sees across its whole duration, not something individual atomic operations provide for free by being individually correct — and multi-version concurrency control delivers that guarantee without needing a lock a writer has to wait on.** By never overwriting data in place — appending a new, timestamped version on every write instead — a read transaction can be pinned to a single snapshot timestamp captured once, at the start, and answer every subsequent read from that fixed point in history. Writes that commit after the snapshot began are simply invisible to it, which is a correctness guarantee, not a blocking mechanism: the writer making those changes never needs to know or care that an old snapshot exists.

## Pros

- Gets the same correctness guarantee as a whole-transaction lock (verified: no read skew, conserved total held in both designs) with none of the writer-blocking cost (verified: 0ms blocked versus ~210ms)
- A snapshot's boundary is precise and explicit (a single timestamp), not an implicit consequence of how long a lock happens to be held
- A later snapshot, taken after a conflicting write commits, correctly sees the new values — this is a timing boundary that moves forward with new transactions, not a permanently stale view

## Cons

- Old versions have to be retained for as long as any active snapshot might still need them, which means a real garbage-collection or compaction process is required to reclaim space once no transaction can see them anymore
- Long-running read transactions can force old versions to be kept around far longer than they otherwise would be, trading the writer-blocking cost of a lock for a storage-growth cost instead
- Concurrent writers that touch the very same row a transaction is trying to update still need a separate conflict-resolution strategy (e.g. first-committer-wins, or serializable snapshot isolation) — MVCC solves reader/writer conflicts, not writer/writer conflicts

## When a Global Lock Is Still Acceptable

- Read transactions are short and infrequent enough that writer stalls stay within an acceptable latency budget, even though the mechanism doesn't scale with read duration or frequency
- The system doesn't need multiple concurrent versions of anything and genuinely benefits from the simplicity of reasoning about a single, always-current value per key
- Write throughput isn't a competing priority — if there are effectively no concurrent writers to block, the lock's real cost never materializes in practice

## Code Examples in This Repo

| Example | Domain | What It Shows |
|---------|--------|--------------|
| [`pressure`](./pressure) | No isolation | A reader's two reads straddle a concurrent transfer, computing a total (250) that never existed; correctness holds for each individual operation but not the transaction |
| [`wrong-direction`](./wrong-direction) | Global lock for the whole transaction | Correct total (200), but a writer touching different accounts is blocked for the reader's entire ~200ms transaction (~210ms measured) |
| [`principle-applied`](./principle-applied) | MVCC (snapshot isolation) | Same correct total (200); writer completes in 0ms with no lock contention; a later snapshot correctly sees the new values |

### How to Run

```bash
cd volume-23-database-internals-for-architects/paper-125-mvcc-snapshot-isolation/pressure
javac *.java && java Main

cd volume-23-database-internals-for-architects/paper-125-mvcc-snapshot-isolation/wrong-direction
javac *.java && java Main

cd volume-23-database-internals-for-architects/paper-125-mvcc-snapshot-isolation/principle-applied
javac *.java && java Main
```
