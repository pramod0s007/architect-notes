# Backpressure — Slowing the Producer Down Is the Feature, Not the Bug

**Continues Volume 16** — picking up right after Paper 86 (Change Data Capture).

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

An unbounded queue between a producer and a permanently slower consumer grows without limit (measured: over 12 ticks, peak queue size already exceeded a safe budget). Bounding the queue and blocking the producer on full applies real backpressure — the queue stays bounded and every event is eventually processed with zero loss (measured: 12 produced, 12 consumed, peak queue size exactly at capacity, at the cost of the producer taking 3x longer). This paper then shows the overcorrection: bounding the queue but dropping instead of blocking on full, which keeps memory flat and the producer unstalled while permanently losing real events (measured: 4 of 12 events silently dropped).

## The Measurements

- **Unbounded queue (pressure)**: peak queue size 9 against a safe limit of 5, over just 12 ticks, with no recovery in sight.
- **Bounded, producer blocks (principle-applied)**: 12 produced, 12 consumed, peak queue size exactly 5, zero data loss — took 36 ticks instead of 12.
- **Bounded, drop on full (wrong-direction)**: 4 of 12 events permanently dropped, while queue size and producer stall metrics look identical to or better than the correct fix.

## The Finding

**A sustained producer/consumer speed mismatch has to go somewhere: unbounded memory, producer latency, or event loss — there is no fourth option.** Only blocking the producer when the queue is full delivers zero data loss. A bounded queue that drops on full looks operationally healthier on every dashboard metric while silently losing real events with no signal that it happened.

## Code Examples in This Repo

| Example | What It Shows |
|---------|--------------|
| [`pressure/`](./pressure/) | Unbounded queue — measured unchecked growth past a safe memory budget |
| [`principle-applied/`](./principle-applied/) | Bounded queue, producer blocks — measured zero data loss at the cost of throttled throughput |
| [`wrong-direction/`](./wrong-direction/) | Bounded queue, drop on full — measured 4 of 12 real events permanently lost |

### How to Run

```bash
cd volume-16-streaming-realtime-data/paper-87-backpressure/pressure
javac *.java && java Main

cd volume-16-streaming-realtime-data/paper-87-backpressure/principle-applied
javac *.java && java Main

cd volume-16-streaming-realtime-data/paper-87-backpressure/wrong-direction
javac *.java && java Main
```
