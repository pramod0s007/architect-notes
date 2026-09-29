# Caching — Checking Expiry and Reloading Must Be One Atomic Step

**Pattern:** No expiration (permanent staleness) → TTL-based expiration (fixes staleness, causes a cache stampede) → Request coalescing (single-flight loads)

**Volume 8b** (Networking & RPC Fundamentals) — a short bridge volume between Volume 8 (Concurrency Patterns) and Volume 9 (Distributed Systems Resilience). Each paper here uses an independent example, in the pre-Volume-9 style.

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

A cache with no expiration and no invalidation serves the first value it ever loaded forever — a price drop applied directly to the backing store never reaches it, no matter how many times it's read afterward (verified). A TTL bounds that staleness, but checks expiry independently per request: 20 threads hitting the same expired key at the same instant produce 20 independent reloads, a cache stampede (verified: 21 total backing-store loads for 20 concurrent readers). The fix isn't a smarter expiry policy — it's making "is this expired, and who reloads it" one atomic decision instead of many, via request coalescing (verified: the identical 20-thread stampede drops to 2 total loads). A first attempt at that fix, using two separate maps for cached values and in-flight loads, was itself found to be racy under real concurrency and had to be corrected to a single atomic map.

## The Pressure: A Cache Is a Promise to Never Ask Again

A cache with no expiration doesn't slowly go stale — it is structurally incapable of noticing that the underlying data changed at all.

## The Principle

**Bounding staleness and eliminating a stampede are two different problems, and solving one doesn't solve the other.** A TTL fixes "how long do I trust a value" but does nothing to coordinate what happens when many concurrent readers stop trusting it at the same instant — that requires making the expiry check and the reload decision a single atomic step, not two separate operations that a delayed thread can slip between.

## Pros

- A TTL cache is simple and correctly bounds staleness to a configurable window
- Request coalescing collapses N concurrent misses into exactly 1 backing-store load, regardless of concurrency level
- A single unified atomic state map avoids the check-then-act race that a two-map design is vulnerable to

## Cons

- A cache with no invalidation is only safe for genuinely immutable data
- Request coalescing means every waiting thread is blocked on one winner's load latency instead of running its own load in parallel
- The atomicity has to cover both the expiry check and the reload decision together — partial fixes reintroduce the exact race they were meant to close

## When NOT to Use (request coalescing)

- The underlying data never changes after it's written — there's no expiry to race on if nothing ever expires
- Concurrent misses on the same key are rare in practice (traffic spread across many distinct keys)
- A brief burst of redundant backing-store load is genuinely cheap and harmless, and the added blocking cost of coalescing isn't worth it

## Code Examples in This Repo

| Example | Domain | What It Shows |
|---------|--------|--------------|
| [`evolution/v1`](./evolution/v1) | No-expiration cache | A price update never reaches the cache; every read serves the original stale value forever |
| [`evolution/v2`](./evolution/v2) | TTL cache | Fixes staleness cleanly, but 20 concurrent readers on an expired key cause 21 backing-store loads |
| [`evolution/v3`](./evolution/v3) | Single-flight (request-coalescing) cache | The identical 20-thread stampede drops to 2 total loads via one atomic `compute()` per key |

### How to Run

```bash
cd volume-8b-networking-rpc-fundamentals/paper-42-caching/evolution/v1
javac *.java && java Main

cd volume-8b-networking-rpc-fundamentals/paper-42-caching/evolution/v2
javac *.java && java Main

cd volume-8b-networking-rpc-fundamentals/paper-42-caching/evolution/v3
javac *.java && java Main
```
