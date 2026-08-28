# CQRS — When Reads and Writes Stop Sharing a Lock

**Opens Volume 11** — one running example, an account ledger, evolved across three data-architecture patterns: CQRS → Event Sourcing → Sharding. Each paper's fix creates the next paper's problem.

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

A ledger where transactions and statements share one lock means a slow statement (walking the full transaction history) blocks a fast transaction (measured: write latency goes from 0ms to ~300-400ms purely from read contention on the same lock). CQRS splits the read model from the write model onto separate locks, eliminating the contention (measured: 0ms regardless of concurrent reads). This paper then shows the overcorrection: replicating writes to the read model asynchronously reintroduces a real staleness window where a customer can't see their own just-completed deposit.

## The Measurements

- **Shared lock**: `transact()` takes 0ms alone, ~300-400ms while a `statement()` holds the same lock.
- **CQRS, separate locks, synchronous propagation**: `transact()` takes 0ms regardless of concurrent reads — contention eliminated, read-your-own-writes intact.
- **CQRS, separate locks, asynchronous propagation**: `transact()` still 0ms, but the read model shows 0 cents immediately after a $100 deposit, only becoming correct 200ms later.

## The Finding

**CQRS's read/write split doesn't require staleness.** Separating the lock and separating the timing are two different decisions — the first removes contention for free; the second trades read-your-own-writes for genuine horizontal scale, and needs to be chosen on purpose, not inherited silently.

## Code Examples in This Repo

| Example | What It Shows |
|---------|--------------|
| [`pressure/`](./pressure/) | Shared model, one lock — measured write latency jumping to ~300-400ms under read contention |
| [`principle-applied/`](./principle-applied/) | CQRS with separate locks, synchronous propagation — measured 0ms write latency regardless of read load |
| [`wrong-direction/`](./wrong-direction/) | CQRS with asynchronous propagation — measured a 200ms window where a customer's own deposit is invisible in the read model |

### How to Run

```bash
cd volume-11-data-architecture-patterns/paper-59-cqrs/pressure
javac *.java && java Main

cd volume-11-data-architecture-patterns/paper-59-cqrs/principle-applied
javac *.java && java Main

cd volume-11-data-architecture-patterns/paper-59-cqrs/wrong-direction
javac *.java && java Main
```
