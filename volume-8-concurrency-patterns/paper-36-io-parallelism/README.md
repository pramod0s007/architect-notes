# I/O Parallelism — Thread-per-Request vs Event Loop

**Concept:** Thread pool sizing for I/O-bound concurrent work (client-side fan-out), and async/non-blocking as a separate axis

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

Paper 32 covered the server side: one event loop thread servicing many inbound connections. This paper covers the client side: a service that fans out to many downstream dependencies concurrently, sized the way a CPU-bound pool usually is (`availableProcessors()`) — which is exactly wrong for I/O-bound work, where a thread spends nearly all its time blocked waiting, not computing. Measured: the same 400 downstream calls complete in ~7x less wall-clock time from a pool-sizing change alone (pool 8 -> pool 64), and a further ~6x less by removing blocking altogether with an async client instead (pool 64 -> async). Compared to the original undersized pool of 8, async end-to-end is roughly ~45x faster — not a 7x-then-45x compounding, just two different baselines for the same number.

## The Pressure: Measuring the Sizing Gap

400 simulated downstream calls, each blocking ~50ms. Pool size 8 (== cores): ~2825-2855ms. Pool size 64: ~388-403ms. Same hardware, same work, ~7x difference from sizing alone.

## The Principle

**Brian Goetz's formula — threads = cores × (1 + wait_time / compute_time) — gives the reasoning, not a number to apply blindly.** CPU-bound work collapses the formula to roughly `cores`. I/O-bound work, where wait time dominates, pushes the ideal size well above core count — capped at actual expected concurrency and what the downstream can absorb, not left uncapped.

## Pros

- A sizing fix requires no change to the actual work being done — same code, dramatically different throughput
- Async/non-blocking removes per-request thread cost entirely — measured, 4 threads managed 400 concurrent in-flight calls faster than 64 blocked threads did
- The formula makes the CPU-bound-vs-I/O-bound distinction explicit and calculable, not a guess

## Cons

- An uncapped pool ("just use more threads") trades an undersized ceiling for an uncontrolled resource leak under a slow-downstream spike
- A bigger pool can just move a capacity problem onto the downstream dependency faster
- Async/non-blocking code is harder to read and debug than straight-line blocking calls

## When NOT to Increase Pool Size

- Genuinely CPU-bound work — more threads than cores adds scheduling overhead, not throughput
- When the downstream dependency can't absorb more concurrent load
- When request volume is already low enough that pool size was never the bottleneck

## Code Examples in This Repo

| Example | Domain | What It Shows |
|---------|--------|--------------|
| [`evolution/`](./evolution/) | Downstream fan-out (simulated blocking I/O) | v1: CPU-bound work, pool size = cores (correct) -> v2: I/O-bound work, pool size 8 vs. 64 measured (~7x difference) -> v3: properly-sized+capped pool, plus an async/non-blocking alternative (4 threads, 400 concurrent in-flight calls) |

### How to Run

```bash
cd volume-8-concurrency-patterns/paper-36-io-parallelism/evolution/v1
javac *.java && java Main

cd volume-8-concurrency-patterns/paper-36-io-parallelism/evolution/v2
javac *.java && java Main

cd volume-8-concurrency-patterns/paper-36-io-parallelism/evolution/v3
javac *.java && java Main
```
