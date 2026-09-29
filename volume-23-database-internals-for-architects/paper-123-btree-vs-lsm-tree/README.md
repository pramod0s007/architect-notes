# B-Tree vs LSM-Tree — Write Amplification and Read Amplification Are Different Failures

**Pattern:** B-Tree (10,000 random-order inserts cost 10,686 scattered node writes, but only 3 node reads per lookup) → LSM-tree with no Bloom filter (the same inserts cost only 20 sequential flushes, but a missing-key lookup jumps to 20 SSTable reads with nothing to rule any of them out) → LSM-tree + Bloom filter per SSTable (same 20 sequential flushes, missing-key lookups drop to 0 actual reads, present-key lookups drop from 20 to 4)

**Volume 23** (Database Internals for Architects) — Paper 123.

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

A database's index structure decides where writes physically land on disk, and that decision has real consequences that don't show up in a query plan. A B-Tree keeps keys sorted in place across nodes at fixed, scattered disk locations — a random-order insert (e.g. a UUID primary key) can land anywhere in that structure, and a full node has to split to make room (verified: 10,000 random-order inserts cost 10,686 total node writes, each landing on an effectively random disk page). An LSM-tree buffers writes in memory and flushes them as one large sequential write instead, which fixes the random-write problem completely (verified: the same 10,000 inserts cost only 20 sequential flush operations) — but without anything recording which SSTable holds which key, a lookup for a key that doesn't exist has to check every SSTable there is (verified: 20 SSTable reads for a missing key, versus the B-Tree's 3 node reads). The real fix pairs each SSTable with a Bloom filter, letting a lookup rule out SSTables in memory before ever reading them from disk (verified: missing-key lookups drop to 0 actual reads, present-key lookups drop from 20 to 4, with the write-side win fully unchanged).

## The Pressure: A B-Tree Pays for Every Random-Order Insert With a Scattered Disk Write

Every insert into a B-Tree can touch its own leaf plus every ancestor node that has to split to make room, and every one of those touched nodes lives at a scattered, essentially random location on disk. Random-access writes are the single most expensive operation a storage device can be asked to do — and a random-order key (a UUID, a hash-based ID) guarantees this cost on every insert.

## The Principle

**Converting random writes into sequential writes, and keeping reads cheap once you've done that, are two separate engineering problems — solving the first doesn't solve the second, and ignoring the second just relocates the bottleneck.** An LSM-tree's sequential-flush design is a genuine, complete fix for the B-Tree's random-write cost — that property holds regardless of how reads are handled. But nothing about buffering writes sequentially tells a later lookup which SSTable actually holds a given key; without that information, a lookup degrades to checking everything. A Bloom filter closes that gap without touching the write path at all: it answers "definitely not here" for free, in memory, ahead of the disk read it's protecting.

## Pros

- Gets the full sequential-write benefit of an LSM-tree with none of the full-scan lookup cost, because the Bloom filter check happens entirely in memory before any disk read
- A Bloom filter's only honest cost is false positives (occasionally reading an SSTable that doesn't have the key) — it never produces false negatives, so a real match is never missed
- Requires no change to the write path or the SSTable format itself — the filter is built once, at flush time, from data that's already being written

## Cons

- Bloom filters cost memory proportional to the number of keys and the desired false-positive rate — real, though usually small compared to the disk I/O they prevent
- Doesn't eliminate compaction: SSTables (and their filters) still accumulate and eventually need periodic background merging to bound total read cost and disk usage
- A badly undersized filter (too few bits per element) still lets false positives dominate, silently eroding the benefit — sizing has to be checked against the actual key count, not assumed

## When a Plain B-Tree Is Still the Right Choice

- The workload is read-heavy, or writes mostly arrive in roughly sorted key order (e.g. an auto-incrementing primary key), where the random-write weakness rarely triggers
- Point lookups and range scans both need to stay fast, and total write volume comfortably fits the storage engine's I/O budget without random-write cost becoming the bottleneck
- The system can't tolerate the operational complexity of background compaction, which an LSM-tree requires and a B-Tree does not

## Code Examples in This Repo

| Example | Domain | What It Shows |
|---------|--------|--------------|
| [`pressure`](./pressure) | B-Tree | 10,000 random-order inserts cost 10,686 scattered node writes; lookups stay cheap at 3 node reads |
| [`wrong-direction`](./wrong-direction) | LSM-tree, no Bloom filter | Same 10,000 inserts cost only 20 sequential flushes, but a missing-key lookup costs 20 SSTable reads |
| [`principle-applied`](./principle-applied) | LSM-tree + Bloom filter per SSTable | Same 20 sequential flushes; missing-key lookups cost 0 actual reads, present-key lookups drop from 20 to 4 |

### How to Run

```bash
cd volume-23-database-internals-for-architects/paper-123-btree-vs-lsm-tree/pressure
javac *.java && java Main

cd volume-23-database-internals-for-architects/paper-123-btree-vs-lsm-tree/wrong-direction
javac *.java && java Main

cd volume-23-database-internals-for-architects/paper-123-btree-vs-lsm-tree/principle-applied
javac *.java && java Main
```
