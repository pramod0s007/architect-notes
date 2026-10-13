# Deadlock — The Bug That Freezes Production

**Concept:** Deadlock (the four Coffman conditions, and how to reliably reproduce + fix one)

---

## Read the Full Article on Medium

[Deadlock — The Bug That Freezes Production](https://medium.com/@replytopramods.aws/deadlock-the-bug-that-freezes-production-d2a17ddc8e7e)

---

## What This Paper Is About

A deadlock doesn't throw, doesn't log, and doesn't show up in a stack trace pointing at the cause — threads simply stop. This paper shows how to deliberately reproduce a deadlock (a two-account funds transfer with inconsistent lock ordering), the four Coffman conditions that must all hold for it to happen, and two independent, correct fixes — plus the "fix" that quietly turns a deadlock into a livelock instead.

## The Pressure: A Transfer Service That Locks in Argument Order

`transfer(from, to, amount)` locks `from` first, then `to`. Two opposite-direction transfers running at the same moment lock the same two accounts in opposite order — a textbook circular wait. Nothing throws; the only way to detect it from outside is a timeout on `Thread.join()`.

## The Principle

**Deadlock requires all four Coffman conditions (mutual exclusion, hold-and-wait, no preemption, circular wait) to hold simultaneously.** Break any single one and deadlock becomes structurally impossible, not just less probable. In practice: order lock acquisition by a stable key (removes circular wait), or use `tryLock()` with a timeout and randomized backoff (removes no-preemption).

## Pros

- Lock ordering is a structural fix — it prevents the deadlock from ever forming, not just recovers from one
- `tryLock()` with backoff works even when call sites don't (or can't) agree on a global lock order
- Both fixes are provably deadlock-free for this specific cycle, and the reasoning generalizes

## Cons

- Lock ordering requires every call site to participate — one caller that skips it reintroduces circular wait
- `tryLock()` without randomized backoff replaces deadlock with livelock — same zero progress, 100% CPU instead of 0%
- Neither fix helps if the real problem is unbounded lock scope (holding a lock across an I/O call, a DB round-trip, etc.) — that's a design problem, not a lock-ordering problem

## When NOT to Worry About This

- Single-lock designs — see "Start Simple" in the article; circular wait needs at least two resources
- Message-passing / actor-style concurrency with no shared mutable state (see Paper 34, later this volume)
- A single coarse global lock — bad for throughput, but structurally deadlock-free

## Code Examples in This Repo

| Example | Domain | What It Shows |
|---------|--------|--------------|
| [`evolution/`](./evolution/) | Funds transfer | Single-lock (safe) -> two-lock argument-order transfer (reliably reproducible deadlock, detected via join timeout) -> two independent fixes (lock ordering, tryLock+backoff) plus a measured naive-retry-without-backoff comparison (80M+ attempts vs. under 1ms) |

### How to Run

```bash
cd volume-8-concurrency-patterns/paper-31-deadlock/evolution/v1
javac *.java && java Main

cd volume-8-concurrency-patterns/paper-31-deadlock/evolution/v2
javac *.java && java Main   # reproduces the deadlock; exits 1 after detecting it

cd volume-8-concurrency-patterns/paper-31-deadlock/evolution/v3
javac *.java && java Main   # both fixes complete cleanly
```
