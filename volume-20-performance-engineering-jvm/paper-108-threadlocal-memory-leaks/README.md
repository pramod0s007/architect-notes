# ThreadLocal Memory Leaks — Cleared at the Wrong Time Is Its Own Kind of Broken

**Continues Volume 20** — picking up right after Paper 107 (JIT Warmup and Cold Start).

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

Data stored in a ThreadLocal without an explicit removal survives for as long as the thread that owns it lives, and a pooled thread's lifetime has no natural relationship to any single request's lifetime (measured: five requests left five entries permanently resident on the same thread). Removing the ThreadLocal state exactly once, at the true end of the request's lifecycle, eliminates the leak with no cost to correctness (measured: the identical five requests left zero residual entries). This paper then shows the overcorrection: clearing the ThreadLocal from inside a shared utility called partway through request handling, rather than at the request's actual end, which breaks every request that depends on the value still being present afterward (measured: the leak was completely fixed and every single request's own later step fell back to a degraded default instead of the value it had itself computed moments earlier).

## The Measurements

- **Never cleared (pressure)**: 5 requests on the same pooled thread leave 5 entries permanently resident — unbounded growth for as long as the thread lives.
- **Cleared once, at request end (principle-applied)**: the identical 5 requests leave 0 residual entries — the leak is eliminated with no cost to correctness.
- **Cleared mid-request by an unrelated utility (wrong-direction)**: 0 residual entries (leak genuinely fixed), but all 5 of 5 requests fall back to a degraded default because their own earlier step's cached value was wiped before their own later step could read it.

## The Finding

**"Something calls remove()" and "remove() runs at the correct point in the lifecycle" are different claims — the first is necessary, the second is the actual requirement, and confirming only the first can leave a codebase that's simultaneously leak-free and broken.**

## Code Examples in This Repo

| Example | What It Shows |
|---------|--------------|
| [`pressure/`](./pressure/) | Never cleared — measured unbounded accumulation on a pooled thread |
| [`principle-applied/`](./principle-applied/) | Cleared once, at request end — measured zero residual entries with no correctness cost |
| [`wrong-direction/`](./wrong-direction/) | Cleared mid-request by an unrelated utility — measured a leak-free but 100%-broken result |

### How to Run

```bash
cd volume-20-performance-engineering-jvm/paper-108-threadlocal-memory-leaks/pressure
javac *.java && java Main

cd volume-20-performance-engineering-jvm/paper-108-threadlocal-memory-leaks/principle-applied
javac *.java && java Main

cd volume-20-performance-engineering-jvm/paper-108-threadlocal-memory-leaks/wrong-direction
javac *.java && java Main
```
