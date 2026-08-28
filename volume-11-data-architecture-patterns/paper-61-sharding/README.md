# Sharding — Partitioning a Ledger That Outgrew One Node

**Closes Volume 11's arc** — picking up right after Paper 60 (Event Sourcing).

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

A single event store node means every account's writes share one lock, capping total throughput regardless of how unrelated the accounts are (measured: 500 concurrent accounts, ~650-730ms total, real Thread timing). Sharding by account ID splits the data and the lock across multiple nodes, removing that ceiling (measured: ~56ms at 500 accounts, ~12x faster). This paper then shows the overcorrection: sharding infrastructure that's identical, but keyed by a poorly-distributed value (region) instead of the account ID, recreating single-node-like contention despite having 16 shards.

## The Measurements

- **Single node**: ~650-730ms total for 500 concurrent accounts appending (real timing, varies run to run).
- **Sharded by account ID (16 shards)**: ~56-57ms total for the same 500 accounts — ~12x faster.
- **Sharded by region (16 shards, wrong key)**: ~650-730ms total for the same 500 accounts — statistically identical to the single-node case, because 98% of this plausible account distribution shares one region.

## The Finding

**Sharding infrastructure without a well-distributed key buys nothing.** The shard key has to be chosen against the real distribution of traffic — a hash of a high-cardinality, evenly-distributed identifier almost always beats a key chosen for a secondary, plausible-sounding reason that turns out to be lopsided in practice.

## Code Examples in This Repo

| Example | What It Shows |
|---------|--------------|
| [`pressure/`](./pressure/) | Single-node event store — measured throughput capped at ~650-730ms for 500 concurrent, unrelated accounts |
| [`principle-applied/`](./principle-applied/) | Sharded by account ID across 16 shards — measured ~56ms for the same 500 accounts, ~12.6x faster |
| [`wrong-direction/`](./wrong-direction/) | Same 16 shards, keyed by region instead of account ID — measured statistically identical to no sharding at all |

### How to Run

```bash
cd volume-11-data-architecture-patterns/paper-61-sharding/pressure
javac *.java && java Main

cd volume-11-data-architecture-patterns/paper-61-sharding/principle-applied
javac *.java && java Main

cd volume-11-data-architecture-patterns/paper-61-sharding/wrong-direction
javac *.java && java Main
```
