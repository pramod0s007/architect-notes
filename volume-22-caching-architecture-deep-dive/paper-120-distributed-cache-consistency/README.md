# Distributed Cache Consistency — TTL Bounds Staleness, It Doesn't Close It

**Pattern:** No invalidation (a write on one node never reaches other nodes' caches — permanent staleness) → TTL-based expiry (bounds staleness to a fixed window, but read-your-own-write still fails inside that window regardless of how short the TTL is) → write-triggered invalidation, broadcast to every node before the write returns (closes the staleness window entirely — zero stale reads, including the read in the same tick as the write)

**Volume 22** (Caching Architecture Deep Dive) — Paper 120.

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

A deployment with more than one cache node — multiple service instances behind a load balancer, each with its own local in-memory cache in front of a shared backend — has a consistency problem a single-node cache never faces: a write on one node has no automatic way to reach any other node's cache. With no invalidation mechanism at all, that's not temporary — it's permanent (verified: 10 of 10 reads on the other node still stale, indefinitely). Adding a TTL bounds the damage (verified: the permanent-staleness failure is gone, the other node refetches by the TTL deadline), but it does nothing to close the window inside that TTL — an entry's expiry is driven by its own age, not by whether a write actually happened, so the admin's immediate read-your-own-write check, at the very same tick as the write, still fails, and 2 of the following 6 polling reads are also still stale before the entry expires (verified). The real fix makes the write itself the trigger: broadcasting an invalidation to every node before the write call returns closes the window entirely, with zero stale reads verified, including the one that happens in the exact same tick as the write.

## The Pressure: No Invalidation, One Node's Cache Goes Stale Forever

Two cache nodes sit in front of the same backend, each with its own local cache — modeling two instances of the same service. A write through one node updates the backend and that node's own cache immediately. The other node has no way to learn anything happened, so it keeps serving whatever it cached before, forever.

## The Principle

**Read-your-own-write is the sharpest test of a consistency mechanism, and age-based expiry always fails it.** A TTL only knows how old its own cached entry is — it has no concept of "a write just happened somewhere." Shortening the TTL shrinks the average staleness window; it can never make the *specific* read that happens right after a write come back fresh, because that read's staleness has nothing to do with time and everything to do with whether the write itself was ever communicated. The fix is inverting the mechanism from reactive (wait for age to force a refresh) to proactive (the write broadcasts its own invalidation, synchronously, before it returns).

## Pros

- Write-triggered invalidation closes the staleness window completely — not "usually fresh," provably zero stale reads, including read-your-own-write
- No tuning tradeoff between "TTL short enough to bound staleness" and "TTL long enough to avoid excess backend load" — the write itself decides when a value goes stale, not a timer
- Dropping the key on invalidation (rather than pushing the new value) means there's exactly one source of truth per read — no risk of two nodes disagreeing about which pushed value is newest

## Cons

- Requires every write path to know about every cache node (or route through a shared invalidation channel), which is more moving parts than a TTL that needs no coordination at all
- If a node is unreachable when the invalidation broadcasts, that node is stale until it comes back — the mechanism needs an explicit story for partial-broadcast failure that a TTL doesn't need, because a TTL bounds staleness independently of any broadcast succeeding
- Adds a dependency from the write path's latency on how many nodes need to be told, whereas a TTL-based write is a pure local operation

## When TTL-Only Expiry Is Still the Right Choice

- The data changes on a schedule that has nothing to do with writes going through this cache layer — an external batch job with no invalidation hook
- A short, known-bounded staleness window is genuinely acceptable for the data in question
- The deployment has a single node, or writes never happen anywhere but through the node serving the read — the whole cross-node staleness problem only exists once more than one node can hold a copy of the same key

## Code Examples in This Repo

| Example | Domain | What It Shows |
|---------|--------|--------------|
| [`pressure`](./pressure) | No invalidation | 10 of 10 reads on the other node stale, permanently |
| [`wrong-direction`](./wrong-direction) | TTL expiry (5 ticks) | Permanent staleness fixed; read-your-own-write still fails, plus 2 of 6 polling reads still stale |
| [`principle-applied`](./principle-applied) | Write-triggered invalidation | 0 of 10 reads stale, read-your-own-write holds |

### How to Run

```bash
cd volume-22-caching-architecture-deep-dive/paper-120-distributed-cache-consistency/pressure
javac *.java && java Main

cd volume-22-caching-architecture-deep-dive/paper-120-distributed-cache-consistency/wrong-direction
javac *.java && java Main

cd volume-22-caching-architecture-deep-dive/paper-120-distributed-cache-consistency/principle-applied
javac *.java && java Main
```
