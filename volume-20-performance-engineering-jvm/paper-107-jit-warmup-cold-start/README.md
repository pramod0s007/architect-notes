# JIT Warmup and Cold Start — A Warmup That Ran Isn't a Warmup That Covered Anything

**Continues Volume 20** — picking up right after Paper 106 (Garbage Collection Pause Tuning).

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

This paper uses an honestly-disclosed, simplified simulation of JIT tiered-compilation behavior (deterministic, in-process Java — not real HotSpot compiler instrumentation), since real JIT compile-threshold behavior isn't observable via plain `javac`/`java` without JVM-flag control. All figures below are from this simulation, not a real production JVM.

An instance that receives real traffic with no warmup pays the full interpreted-mode compilation cost directly on real users, and a code path that never generates enough volume on its own never compiles at all during that instance's lifetime (simulated: 2000 real requests averaged 40.4ms, with one path staying interpreted for its entire life). Warming every real code path with synthetic traffic before accepting real requests pays the interpreted cost once, up front, instead of spreading it across real users (simulated: the identical traffic mix dropped to a 2.0ms average). This paper then shows the overcorrection: a warmup script that only covers some of the real traffic's code paths, leaving the uncovered majority exactly as cold as no warmup at all while a readiness gate reports success (simulated: warming only the minority path left a 26.0ms average, far closer to the no-warmup case than to a correctly warmed one).

## The Measurements

- **No warmup (pressure)**: 2000 real requests across two paths average 40.4ms — one path (600 requests) never reaches the compile threshold and stays interpreted for its entire lifetime.
- **Warmup covers both real paths (principle-applied)**: the identical real traffic mix averages 2.0ms, because both paths were pre-compiled by 1000 synthetic invocations each before any real request arrived.
- **Warmup covers only one path (wrong-direction)**: a readiness gate reports "warmup complete" after only exercising the minority path — the majority path (1400 of 2000 requests, 70%) is never touched, and real traffic averages 26.0ms, barely better than no warmup at all.

## The Finding

**"The warmup step ran" and "the warmup step covered what real traffic actually needs" are different claims, and a readiness check that only verifies the first one provides no real evidence about the second.**

## Code Examples in This Repo

| Example | What It Shows |
|---------|--------------|
| [`pressure/`](./pressure/) | No warmup — simulated real users paying the full compilation cost, 40.4ms average |
| [`principle-applied/`](./principle-applied/) | Warmup covers every real path — simulated a 2.0ms average from the first real request |
| [`wrong-direction/`](./wrong-direction/) | Warmup covers only the minority path — simulated a 26.0ms average despite a passing readiness gate |

### How to Run

```bash
cd volume-20-performance-engineering-jvm/paper-107-jit-warmup-cold-start/pressure
javac *.java && java Main

cd volume-20-performance-engineering-jvm/paper-107-jit-warmup-cold-start/principle-applied
javac *.java && java Main

cd volume-20-performance-engineering-jvm/paper-107-jit-warmup-cold-start/wrong-direction
javac *.java && java Main
```
