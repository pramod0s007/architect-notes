# Actor Pattern — Concurrency Without Shared State

**Concept:** Actor model (private state + mailbox + single worker thread; no locks, but a different deadlock is still possible)

---

## Read the Full Article on Medium

[Actor Pattern — Concurrency Without Shared State](https://medium.com/@replytopramods.aws/actor-pattern-concurrency-without-shared-state-ea0fa60af1df)

---

## What This Paper Is About

Paper 31's funds-transfer deadlock came from two threads racing for two locks. This paper rebuilds the exact same transfer as actors — each account's state is touched only by its own single worker thread, so there's nothing to lock. Then it shows the one way actors can still deadlock: blocking synchronously for a reply from within an actor's own message handler, which has nothing to do with locks at all.

## The Pressure: Synchronous "Ask" Self-Deadlocks

An actor's mailbox + worker thread is a single-threaded executor. Submitting a task to that executor and then blocking (unconditionally) on its result from *within* a task already running on it is a guaranteed, deterministic deadlock — verified: "inner task" never prints, on any run.

## The Principle

**An actor's state needs no lock because no thread other than its own ever touches it.** This eliminates lock-based circular-wait deadlock. It does not eliminate deadlock in general — a blocking "ask" reintroduces a structurally different kind. The fix is always the same shape: chain continuations ("tell, then queue the next step") instead of blocking for a reply.

## Pros

- No lock, ever, for an actor's own state — removes an entire class of deadlock by construction
- Trivially solves Paper 33's multi-field-invariant problem — both fields live on one actor, one thread, one message
- Debugging a single actor's state changes is straightforward — one thread, one queue, processed in order

## Cons

- One worker thread per actor means an actor never parallelizes its own work across cores
- Logic is split across chained callbacks instead of a single linear call stack
- A blocking "ask" from within a handler self-deadlocks a single-threaded executor — deterministically, not probabilistically

## When NOT to Use Actors

- CPU-bound work that needs real parallelism — belongs on a thread/task pool, not inside one actor's handler
- When a caller genuinely needs a synchronous result before proceeding — forces exactly the blocking pattern shown to self-deadlock
- Simple, low-contention state that a single `synchronized` method already handles cleanly

## Code Examples in This Repo

| Example | Domain | What It Shows |
|---------|--------|--------------|
| [`evolution/`](./evolution/) | Funds transfer (same scenario as Paper 31) | v1: one actor, 1000 concurrent callers, zero races -> v2: synchronous "ask" self-deadlocks a single-threaded executor, confirmed unconditional -> v3: async continuation-chained transfer, no lock, no deadlock, correct balances |

### How to Run

```bash
cd volume-8-concurrency-patterns/paper-34-actor-pattern/evolution/v1
javac *.java && java Main

cd volume-8-concurrency-patterns/paper-34-actor-pattern/evolution/v2
javac *.java && java Main   # takes ~3s: demonstrates the self-deadlock, then recovers via shutdownNow()

cd volume-8-concurrency-patterns/paper-34-actor-pattern/evolution/v3
javac *.java && java Main
```
