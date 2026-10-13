# Reactor Pattern — Non-Blocking I/O Done Right

**Concept:** Reactor pattern (single-threaded event loop over `java.nio.channels.Selector`)

---

## Read the Full Article on Medium

[Reactor Pattern — Non-Blocking I/O Done Right](https://medium.com/@replytopramods.aws/reactor-pattern-non-blocking-i-o-done-right-c56687b42114)

---

## What This Paper Is About

Thread-per-connection is simple and correct at small scale, but its cost scales with connection *count*, not with actual traffic — every idle connection still holds a full OS thread and stack. This paper measures that cost directly (2,000 real threads, most doing nothing), then replaces it with a single-threaded reactor built on `Selector`, where one thread services many channels by asking the OS which ones are actually ready.

## The Pressure: Measuring Thread-per-Connection's Real Cost

2,000 simulated idle connections, each with its own thread blocked in `read()`, cost real, measurable startup time and reserve roughly a gigabyte of stack space — for connections that never send a single byte.

## The Principle

**A Reactor uses one (or a few) threads and an OS-level readiness notification (`Selector.select()`) to service many I/O channels, touching only the ones that actually have data.** Cost becomes proportional to active traffic, not to how many connections happen to be open.

## Pros

- Cost scales with actual activity, not connection count — a reactor with 50,000 idle channels costs about the same as one with 50
- One thread, so no thread-scheduling overhead as connection count grows
- `Selector.select()` blocks efficiently at the OS level — it's not a spin loop

## Cons

- A handler that blocks stalls every other channel registered on that selector — blocking work must be offloaded elsewhere
- Debugging is harder: one connection's logic is no longer a single linear stack trace
- Naive re-implementations (polling every channel without `select()`) trade a thread-count problem for a CPU-burning one

## When NOT to Use

- Low connection counts with long-running, CPU-heavy per-connection work — thread-per-connection's isolation and simplicity win
- Any workload where handlers might block — a reactor's entire value depends on the loop thread never blocking
- When debuggability during incidents matters more than raw connection-count scale

## Code Examples in This Repo

| Example | Domain | What It Shows |
|---------|--------|--------------|
| [`evolution/`](./evolution/) | Long-lived connections | v1: thread-per-connection at small scale (fine) -> v2: 2,000 threads, measured cost -> v3: single-threaded `Selector`-based reactor servicing 20 channels with exactly 1 thread |

**Note on v3:** uses `java.nio.channels.Pipe` (in-process channels) instead of real network sockets, so the demo never binds a port or touches the network — same `Selector`/`SelectionKey`/`register()` mechanics apply identically to `SocketChannel` in production.

### How to Run

```bash
cd volume-8-concurrency-patterns/paper-32-reactor-pattern/evolution/v1
javac *.java && java Main

cd volume-8-concurrency-patterns/paper-32-reactor-pattern/evolution/v2
javac *.java && java Main

cd volume-8-concurrency-patterns/paper-32-reactor-pattern/evolution/v3
javac *.java && java Main
```
