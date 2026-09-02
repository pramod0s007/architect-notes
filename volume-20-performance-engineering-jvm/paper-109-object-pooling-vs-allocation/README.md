# Object Pooling vs Allocation — The Technique That Saved You Real Time Can Cost You Real Time

**Continues Volume 20** — picking up right after Paper 108 (ThreadLocal Memory Leaks).

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

Allocating a genuinely expensive object -- one that requires claiming and zeroing a large amount of memory -- fresh on every use pays a real, measurable, repeated cost for work that never needed to happen more than once (measured: 268ms on one run, consistently 190-340ms across repeated runs, for 20,000 fresh 50,000-element buffer allocations). Reusing that same buffer instead of reallocating it collapses the measured cost to effectively nothing (measured: 0ms for the identical 20,000 logical uses). This paper then shows the overcorrection: applying the identical pooling technique to a small, cheap-to-allocate object, where the synchronization required to share pooled instances safely costs measurably more than the near-free allocation it was meant to avoid (measured: pooling was roughly 5x slower than plain allocation, across 20,000,000 iterations).

## The Measurements

- **Fresh allocation, expensive object (pressure)**: 20,000 iterations allocating a fresh 50,000-element buffer every time — 268ms on one run, 190-340ms across repeated runs.
- **Reused, expensive object (principle-applied)**: the identical 20,000 logical uses, buffer allocated once and reused — 0ms.
- **Fresh allocation vs. pooled, cheap object (wrong-direction)**: 20,000,000 iterations of plain allocation (14ms) vs. synchronized pool acquire/release (74ms) for an identical two-field object — pooling measured roughly 5x slower.

## The Finding

**Whether pooling helps or hurts depends entirely on whether the object being pooled is actually expensive to allocate -- the same code pattern produces a dramatic, measured win for one object and a measured, repeatable loss for another.**

## Code Examples in This Repo

| Example | What It Shows |
|---------|--------------|
| [`pressure/`](./pressure/) | Fresh allocation of a genuinely expensive object every iteration — measured real, repeated cost |
| [`principle-applied/`](./principle-applied/) | Reusing the one expensive part instead of reallocating it — measured cost collapsing to effectively zero |
| [`wrong-direction/`](./wrong-direction/) | Pooling a cheap object that was never expensive to allocate — measured synchronization overhead costing more than the allocation it replaced |

### How to Run

```bash
cd volume-20-performance-engineering-jvm/paper-109-object-pooling-vs-allocation/pressure
javac *.java && java Main

cd volume-20-performance-engineering-jvm/paper-109-object-pooling-vs-allocation/principle-applied
javac *.java && java Main

cd volume-20-performance-engineering-jvm/paper-109-object-pooling-vs-allocation/wrong-direction
javac *.java && java Main
```
