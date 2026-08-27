# Sync Primitives — Locks, Semaphores, and When They Lie to You

**Concept:** Visibility vs. atomicity (`volatile`, `synchronized`, `Atomic*`, and why `volatile` alone doesn't fix a race)

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

`volatile` looks like the fix for a race condition on a counter. It isn't — it solves visibility (every thread sees the latest write), not atomicity (whether a multi-step operation like `count++` can be interrupted mid-way by another thread). This paper reproduces the lost-update bug at scale, proves `volatile` doesn't fix it, and shows the two primitives that do — plus a subtler bug where locking each field separately still fails to protect the invariant between them.

## The Pressure: volatile Doesn't Fix Compound Operations

20 threads, 2,500 increments each, expected total 50,000. A plain `int` and a `volatile int` both drift — differently every run — because `count++` is three steps (read, compute, write) regardless of the field's visibility guarantee.

## The Principle

**Visibility and atomicity are different guarantees.** `volatile` gives you visibility only. `synchronized`/locks give you both via mutual exclusion. `Atomic*` types give you both for a single variable without blocking, via a compare-and-swap retry loop.

## Pros

- `AtomicInteger` and friends give lock-free atomicity — no thread ever blocks, contention shows up as a retry
- `synchronized`/`ReentrantLock` scale to multi-field invariants that a single `Atomic*` variable can't express
- Already-synchronized collections (`ConcurrentHashMap`, `BlockingQueue`) are both safer and usually faster than externally locking a plain collection

## Cons

- `Atomic*` types only protect a single variable — a relationship spanning two fields still needs a lock around the whole operation
- Locking each field independently is a false sense of safety — it doesn't protect the invariant between fields, demonstrated directly in this paper
- CAS retry loops waste cycles under very high contention, same as any optimistic-concurrency approach

## When NOT to Reach for a Heavier Primitive

- A single flag checked once per iteration — `volatile boolean` is correct and sufficient, since the read itself is already one atomic step
- Immutable, safely-published data needs no synchronization to read
- Don't wrap an already-synchronized collection's own methods in your own external lock

## Code Examples in This Repo

| Example | Domain | What It Shows |
|---------|--------|--------------|
| [`evolution/`](./evolution/) | Request-in-flight counter | v1: single-threaded (fine) -> v2: plain int AND volatile int both lose updates under 20-thread contention (measured) -> v3: `synchronized` and `AtomicInteger` both fix it (measured), plus a live demonstration of the per-field-locking invariant bug |
| [`semaphore/`](./semaphore/) | Connection pool | `Semaphore(3)` bounding 20 concurrent callers to exactly 3 simultaneous holders, measured |

### How to Run

```bash
cd volume-8-concurrency-patterns/paper-33-sync-primitives/evolution/v1
javac *.java && java Main

cd volume-8-concurrency-patterns/paper-33-sync-primitives/evolution/v2
javac *.java && java Main

cd volume-8-concurrency-patterns/paper-33-sync-primitives/evolution/v3
javac *.java && java Main

cd volume-8-concurrency-patterns/paper-33-sync-primitives/semaphore
javac *.java && java Main
```
