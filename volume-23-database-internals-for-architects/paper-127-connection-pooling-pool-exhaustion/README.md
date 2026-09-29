# Connection Pooling & Pool Exhaustion — A Pool Only Helps If Every Connection Comes Back

**Pattern:** No connection pooling (a burst of 12 requests against a database with a hard limit of 5 — 5 get through, 7 refused outright) → a pool with a connection leak (5 ordinary requests, each acquiring but never releasing, exhaust the pool completely; a 6th request times out) → a pool with guaranteed release via try/finally (the same 5-connection pool serves 20 requests, 4 of which deliberately fail, ending with all 5 connections back and available)

**Volume 23** (Database Internals for Architects) — Paper 127.

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

A connection pool exists to amortize connection setup cost and bound how many real connections a database ever sees — but the pool's entire value proposition depends on a guarantee it can't enforce by itself: that every connection borrowed from it actually comes back. Without any pool at all, a burst of requests each opening its own direct connection runs straight into the database's own hard connection limit, with no way for the application to smooth the burst (verified: a burst of 12 requests against a limit of 5 — only 5 get through, 7 are refused outright). Adding a pool looks like the fix, but a missing `finally` in the caller's code — acquiring a connection to do work and never releasing it afterward — reproduces exhaustion with only ordinary load and zero unusual traffic (verified: 5 completely normal requests leak all 5 connections, and a 6th request times out waiting ~209ms, a symptom indistinguishable from genuine overload). The actual fix makes release unconditional: wrapping the borrow in try/finally so the connection returns whether the request's work succeeds or throws (verified: the same 5-connection pool serves 20 requests, 4 of which deliberately fail mid-work, and ends with all 5 connections available again).

## The Pressure: No Pooling Means a Burst Hits the DB's Hard Limit

A database's own connection limit is a hard, real constraint enforced entirely outside the application. Without a pool or any admission-control layer in front of it, the application has no way to queue, smooth, or reuse connections across a burst of demand — whatever fraction of requests happens to arrive before the limit is hit gets served, and the rest are refused outright, with the split determined by arrival timing rather than anything the application decided.

## The Principle

**A connection pool's benefit comes entirely from a guarantee it depends on but cannot enforce itself: that a borrowed connection is always returned, regardless of what happens while it's borrowed.** Adding a pool without that guarantee doesn't shrink the original problem — it creates a different, more insidious one, because a leaked connection converts a transient event (one request's work) into a permanent, silent loss of capacity, with no external signal distinguishing it from genuine overload. The fix isn't a pool-level setting; it's structural: wrapping every acquire in a try/finally (or try-with-resources) so release happens unconditionally, whether the request's work succeeds or throws.

## Pros

- Amortizes connection setup cost and bounds how many real connections the database ever sees, verified serving 20 requests with only 5 actual connections
- Guaranteed release via try/finally makes the pool robust to request-level failures — an error in one request's work doesn't cost the pool a connection, verified across 4 deliberately failing requests
- A bounded `acquire(timeout)` gives the application an explicit, observable signal (a timeout) when demand genuinely exceeds capacity, rather than either refusing instantly or blocking forever

## Cons

- A pool adds a layer of state and configuration (size, timeout, health checks) that a direct-connection approach doesn't have, and that state can itself become a source of bugs (as the leak scenario shows)
- Pool exhaustion from a leak looks identical, from the outside, to genuine overload — diagnosing which one occurred requires actually inspecting whether connections are being returned, not just whether requests are timing out
- Sizing the pool still matters: a pool that's too small relative to genuine sustained concurrency will queue or time out requests even with a perfect borrow/return contract

## When Direct, Unpooled Connections Are Still Acceptable

- Request volume and concurrency are low enough, and connection setup cost is cheap enough, that amortizing it isn't worth the added complexity of a pool
- A workload that's inherently single-connection or single-threaded (e.g. a batch job that opens one connection and uses it for its entire run) has no concurrent-access problem for a pool to solve
- The database's own connection limit is high enough relative to realistic peak concurrency that hitting it is not a practical risk

## Code Examples in This Repo

| Example | Domain | What It Shows |
|---------|--------|--------------|
| [`pressure`](./pressure) | No pooling | A burst of 12 requests against a database limit of 5 — 5 get connections, 7 are refused outright |
| [`wrong-direction`](./wrong-direction) | Pool with a connection leak | 5 ordinary requests leak all 5 connections (missing try/finally); a 6th request times out waiting ~209ms |
| [`principle-applied`](./principle-applied) | Pool with guaranteed release | The same 5-connection pool serves 20 requests (4 deliberately erroring) with try/finally, ending with all 5 connections available |

### How to Run

```bash
cd volume-23-database-internals-for-architects/paper-127-connection-pooling-pool-exhaustion/pressure
javac *.java && java Main

cd volume-23-database-internals-for-architects/paper-127-connection-pooling-pool-exhaustion/wrong-direction
javac *.java && java Main

cd volume-23-database-internals-for-architects/paper-127-connection-pooling-pool-exhaustion/principle-applied
javac *.java && java Main
```
