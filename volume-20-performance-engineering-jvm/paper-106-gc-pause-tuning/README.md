# Garbage Collection Pause Tuning — A Short Load Test Will Recommend the Worst Configuration

**Opens Volume 20** — Performance Engineering Deep Dive (JVM), following on from Volume 19 (Platform Engineering & Developer Experience).

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

This paper uses an honestly-disclosed, simplified simulation of old-generation GC behavior (deterministic, in-process Java — not real JVM GC instrumentation), since real GC pause behavior isn't observable via plain `javac`/`java` without JVM-flag control. All figures below are from this simulation, not a real production JVM.

An old generation with barely any headroom above the live working set forces constant collection, and the cumulative overhead of many individually-small pauses collapses throughput even with no single catastrophic pause (simulated: 35% of total time spent in GC across 50 pauses of 70ms each). Sizing old gen from the application's real working set and a real latency SLA reduces total overhead and bounds the worst-case single pause at the same time (simulated: overhead dropped to 12.5% while the max single pause stayed under a 300ms SLA). This paper then shows the overcorrection: a massively oversized old generation that defers collection long enough to look perfect in any observation window short enough to be practical, while the deferred pause grows in direct proportion (simulated: a 100-tick window showed zero GCs, and the same workload's first actual collection cost 33x the service's own latency SLA in a single pause).

## The Measurements

*(All figures are from the disclosed simulation described above, not a real JVM.)*

- **Barely any headroom (pressure)**: capacity 70 vs. a live working set of 50 — 50 full GCs fire over 100 ticks, 35.0% of total time spent collecting.
- **Sized from working set + SLA (principle-applied)**: capacity 250 — 5 full GCs fire over the identical window, 12.5% overhead, max pause 250ms stays under a 300ms SLA.
- **Massively oversized (wrong-direction)**: capacity 10,050 — 0 GCs observed over the same 100-tick window (looks best of all three), but the first real collection, once it finally fires, costs 10,050ms — 33x over the SLA.

## The Finding

**A load test or monitoring window can only report on collections that happen to occur inside it — it cannot distinguish "this configuration doesn't need to collect" from "this configuration hasn't been forced to collect yet," and an oversized heap is specifically engineered to produce the second thing while looking like the first.**

## Code Examples in This Repo

| Example | What It Shows |
|---------|--------------|
| [`pressure/`](./pressure/) | Barely any headroom — simulated 35% GC overhead collapsing throughput |
| [`principle-applied/`](./principle-applied/) | Sized from working set + SLA — simulated overhead and worst-case pause both bounded together |
| [`wrong-direction/`](./wrong-direction/) | Massively oversized — simulated a perfect short-window result followed by a 33x-over-SLA pause |

### How to Run

```bash
cd volume-20-performance-engineering-jvm/paper-106-gc-pause-tuning/pressure
javac *.java && java Main

cd volume-20-performance-engineering-jvm/paper-106-gc-pause-tuning/principle-applied
javac *.java && java Main

cd volume-20-performance-engineering-jvm/paper-106-gc-pause-tuning/wrong-direction
javac *.java && java Main
```
