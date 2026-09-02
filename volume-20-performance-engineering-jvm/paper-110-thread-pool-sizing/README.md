# Thread Pool Sizing — The Fix for Blocked Threads Is the Cause of Wasted Cores

**Continues Volume 20** — picking up right after Paper 109 (Object Pooling vs Allocation).

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

Sizing a thread pool at one thread per core for a workload that spends most of its time blocked -- waiting on a downstream call -- leaves cores idle and work queued behind threads that are only waiting (measured: 226ms for 64 mostly-blocked tasks on a 16-thread pool). Sizing that same pool for the actual wait-to-compute ratio of the task collapses the measured wall-clock time to a fraction of the original, because blocked threads don't compete for cores (measured: the identical 64 tasks at roughly 66ms once the pool was sized at cores × 10). This paper then shows the overcorrection: applying the identical oversized-pool fix to a purely CPU-bound task, where every additional runnable thread beyond the core count adds real scheduling and cache-contention overhead with no extra core to absorb it (measured: about 18% slower than one-thread-per-core, as the median of 7 trials).

## The Measurements

- **One thread per core, mostly-blocked tasks (pressure)**: 64 IO-bound tasks (45ms blocking wait each), 16-thread pool — 226ms, ~220-235ms across repeated runs.
- **Cores × (1 + wait/compute), same tasks (principle-applied)**: the identical 64 tasks, 160-thread pool — 66ms, ~65-75ms across repeated runs.
- **One thread per core vs. cores × 50, CPU-bound tasks (wrong-direction)**: 640 purely CPU-bound tasks, median of 7 trials each — 70ms at pool size 16 vs. 83ms at pool size 800, roughly 18% slower with the oversized pool.

## The Finding

**Whether more threads help or hurt depends entirely on what the threads spend their time doing -- blocked threads waiting on something else can be added cheaply, but a thread actually competing for a core cannot.**

## Code Examples in This Repo

| Example | What It Shows |
|---------|--------------|
| [`pressure/`](./pressure/) | One thread per core for mostly-blocked tasks — measured cores sitting idle behind queued work |
| [`principle-applied/`](./principle-applied/) | Pool sized for the wait-to-compute ratio — measured wall-clock time collapsing toward the cost of one task |
| [`wrong-direction/`](./wrong-direction/) | The same oversized pool applied to purely CPU-bound work — measured scheduling overhead making identical work slower |

### How to Run

```bash
cd volume-20-performance-engineering-jvm/paper-110-thread-pool-sizing/pressure
javac *.java && java Main

cd volume-20-performance-engineering-jvm/paper-110-thread-pool-sizing/principle-applied
javac *.java && java Main

cd volume-20-performance-engineering-jvm/paper-110-thread-pool-sizing/wrong-direction
javac *.java && java Main
```
