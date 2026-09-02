# Partitioning Strategy — When the Right Key Isn't the Same as the Balanced Key

**Opens Volume 16** — Streaming & Real-Time Data Architecture, following on from Volume 15 (Consensus & Distributed Coordination).

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

A real, meaningful business key can still cause severe partition skew if it's low-cardinality (measured: region-keyed partitioning put 24 of 30 events on a single partition, a 2.4x wall-clock penalty). Swapping to a high-cardinality, uniformly distributed key fixes the imbalance without weakening the ordering guarantee (measured: userId-keyed partitioning achieved an exact 10/10/10 split while still routing every user's events to one partition). This paper then shows the overcorrection: dropping the key entirely and partitioning by round-robin send position, which achieves even better load balance and silently breaks per-key ordering (measured: a user's two sequential status updates landed on different partitions with different consumer speeds, and the earlier update was applied last, corrupting the recorded final state).

## The Measurements

- **Region key (pressure)**: 24 of 30 events land on partition 2 — that consumer becomes a 24ms bottleneck while the other two sit mostly idle.
- **UserId key (principle-applied)**: exactly 10/10/10 across 3 partitions — 10ms wall-clock, 2.4x faster than region-keyed, identical ordering guarantee preserved per user.
- **Round-robin (wrong direction)**: mathematically perfect 10/10/10 balance, but a user's SHIPPED update (sent first) is processed at tick 6ms while their CANCELLED update (sent second) is processed at tick 3ms — the final recorded status is SHIPPED, the wrong one.

## The Finding

**Partitioning exists to serve two requirements at once: spreading load, and guaranteeing a single consumer processes all of one entity's events in the order they were sent.** A high-cardinality key satisfies both. Removing the key to chase perfect balance satisfies only the first — and the failure is invisible to any metric that only counts events per partition.

## Code Examples in This Repo

| Example | What It Shows |
|---------|--------------|
| [`pressure/`](./pressure/) | Region key — measured 24 of 30 events on one partition, a 24ms bottleneck |
| [`principle-applied/`](./principle-applied/) | UserId key — measured an exact 10/10/10 split, 2.4x faster, ordering preserved |
| [`wrong-direction/`](./wrong-direction/) | Round-robin, no key — measured perfect load balance and a same-user update applied out of order |

### How to Run

```bash
cd volume-16-streaming-realtime-data/paper-84-partitioning-strategy/pressure
javac *.java && java Main

cd volume-16-streaming-realtime-data/paper-84-partitioning-strategy/principle-applied
javac *.java && java Main

cd volume-16-streaming-realtime-data/paper-84-partitioning-strategy/wrong-direction
javac *.java && java Main
```
