# Negative Caching & Cache Poisoning — Efficient and Correct Are Separate Claims Too

**Pattern:** No negative caching (100 repeated lookups for a missing key cost 100 backend calls) → negative caching with no invalidation (collapses to 1 backend call, but poisons the cache permanently once the record is later created — 10 of 10 subsequent lookups still return stale "not found") → negative caching with write-triggered invalidation on create (keeps the 1-backend-call efficiency, and cuts stale reads to 0 of 10)

**Volume 22** (Caching Architecture Deep Dive) — Paper 122.

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

Negative caching memoizes the result of a lookup that came back empty, so repeated lookups for the same missing key don't each pay the full backend cost — genuinely useful against a scanner probing IDs or a client polling for a record that hasn't appeared yet. Without any negative caching at all, 100 repeated lookups for a missing key cost 100 backend calls (verified). Caching the miss with no expiry and no invalidation fixes that completely (verified: 1 backend call), but a negative cache entry is a claim about the past, not a permanent fact — once the record is actually created, nothing tells the cache, so it keeps serving stale "not found" indefinitely (verified: 10 of 10 lookups after creation still stale). The fix isn't giving up the efficiency: making the create operation invalidate its own negative entry gets both properties at once — the same 1-backend-call efficiency, and zero stale reads after creation (verified).

## The Pressure: No Negative Caching, Every Miss Hits the Backend

Every lookup for a key that doesn't exist pays full backend cost, even when the exact same missing key is looked up repeatedly — a polling client or an ID scanner can turn "this doesn't exist" into sustained, unnecessary backend load.

## The Principle

**A negative cache entry is a claim about the past, not a permanent fact — and nothing about a read-only cache ever revisits that claim.** An unbounded negative cache genuinely fixes the load problem, and that fix is real and verifiable on its own terms. But "this key doesn't exist" is only true at the moment it was checked; a cache that only ever handles reads has no mechanism to notice that a later write made its own earlier claim false. The fix is making the one operation that actually changes whether the claim is still true — the write that creates the record — responsible for retracting it, in the same call. This is the same write-triggered-invalidation mechanism used in Paper 120 (broadcasting a write to every node's cache), applied here to a negative cache entry instead of a positive one: same trigger, different failure it closes.

## Pros

- Gets the full backend-load reduction of unbounded negative caching with none of the staleness risk, because invalidation is tied to the exact operation that invalidates it logically
- Requires no new infrastructure — `create()` was always a distinct operation from `lookup()`; the fix only adds one cache-clearing line to a path that already exists
- Generalizes beyond "create": any write that changes whether a key exists can invalidate the same way, using the same mechanism

## Cons

- Only works if every path capable of creating the record actually goes through the cache's `create()` — a create path that bypasses the cache reintroduces the exact poisoning this fix closes
- Doesn't protect against a cache instance that never receives the invalidation (e.g., a partition or a dropped message in a distributed cache) — a short TTL on negative entries is a reasonable backstop layered on top
- Adds a small bit of coupling between the write path and the cache layer that a purely read-side cache wouldn't otherwise need

## When Unbounded Negative Caching (No Invalidation) Is Still Acceptable

- The set of valid keys is closed and effectively never grows, so "doesn't exist" is a permanent fact by construction
- Every path capable of creating a record is verified to go through the same cache layer, and that's enforced, not just assumed
- A short, explicit TTL on negative entries specifically is layered on top as a backstop, bounding the damage even if an invalidation path is ever missed

## Code Examples in This Repo

| Example | Domain | What It Shows |
|---------|--------|--------------|
| [`pressure`](./pressure) | No negative caching | 100 lookups for a missing key cost 100 backend calls |
| [`wrong-direction`](./wrong-direction) | Negative caching, no invalidation | 100 lookups cost 1 backend call, but 10 of 10 lookups after the record is created still return stale "not found" |
| [`principle-applied`](./principle-applied) | Negative caching + invalidate on create | 100 lookups still cost 1 backend call, and 0 of 10 lookups after creation are stale |

### How to Run

```bash
cd volume-22-caching-architecture-deep-dive/paper-122-negative-caching-cache-poisoning/pressure
javac *.java && java Main

cd volume-22-caching-architecture-deep-dive/paper-122-negative-caching-cache-poisoning/wrong-direction
javac *.java && java Main

cd volume-22-caching-architecture-deep-dive/paper-122-negative-caching-cache-poisoning/principle-applied
javac *.java && java Main
```
