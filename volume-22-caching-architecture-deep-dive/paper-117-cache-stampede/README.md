# Cache Stampede — A Jittered TTL Isn't a Proven Window

**Pattern:** Fixed TTL, bulk warm-up (all keys expire at the exact same instant — a full stampede against uncontended, distinct keys) → random jitter with a guessed window (a real, measurable improvement that still isn't checked against actual backend capacity) → deterministically computed stagger window, `ceil(keyCount / capacity)` (peak guaranteed at or under capacity by construction)

**Volume 22** (Caching Architecture Deep Dive) — Paper 117.

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

A cache stampede across many keys is a different failure than the single-flight / request-coalescing problem covered earlier in this series (Paper 42): single-flight fixes multiple concurrent requests racing to recompute the *same* key, but a stampede is many *different* keys, all uncontended, all expiring at correlated instants because they were populated together with an identical fixed TTL (verified: 1000 keys warmed at tick 0 all expire at the same tick, producing a peak of 1000 simultaneous backend calls against a capacity of 50). Adding random jitter to the TTL is a real, measurable fix — it genuinely spread those 1000 keys across 11 distinct ticks and dropped the peak to 115 — but the jitter window was chosen "by feel," not sized against the actual key count or backend capacity, so it still failed (verified: peak of 115, still more than double the safe capacity of 50). The real fix computes the minimum stagger window the key population actually needs, `ceil(keyCount / capacity)`, and staggers expiry deterministically across it, guaranteeing the peak never exceeds capacity rather than merely observing that it usually doesn't (verified: exactly 50 calls at the busiest tick, across a computed window of 20 ticks).

## The Pressure: One TTL, One Instant, One Pile-Up

A bulk cache warm-up — after a deploy, a cache flush, or a scheduled warming job — populates many keys at the same instant, all with an identical fixed TTL. Every one of them expires at exactly the same tick, so every one of them triggers a backend recompute call at exactly the same instant. No two of those calls are for the same key, so a per-key single-flight lock provides zero protection here — the overload is the sheer count of distinct keys expiring together, not contention on any one of them.

## The Principle

**A stagger window's safety has to be derived from the math of the problem, not observed after the fact.** Random jitter is a genuine improvement over no jitter at all, but "the peak dropped" and "the peak is now under capacity" are two different claims, and a dashboard showing a green trend line can mask the second one being false. The correct window size is `ceil(keyCount / backendCapacityPerTick)` — the minimum number of ticks needed so that no tick receives more keys than the backend can safely absorb — and deterministic staggering across that window turns "usually fine" into "guaranteed fine by construction."

## Pros

- The peak load at any single tick is a provable upper bound, not an empirical observation that could differ on the next bulk warm-up
- The formula is trivial to recompute whenever key count or backend capacity changes, unlike a jitter window that has to be re-tuned by trial and error
- Deterministic staggering produces a perfectly even distribution — no tick is ever busier than any other by chance

## Cons

- Requires knowing (or conservatively estimating) the backend's real safe capacity per tick, which a "just add some jitter" fix doesn't require
- A purely deterministic stagger (e.g., `keyIndex % window`) can create predictable patterns if key population size itself changes at a predictable cadence — mixing in bounded randomness within each staggered slot avoids that without reintroducing the original problem
- Doesn't help if the bulk warm-up's key count is unknown or unbounded at warm-up time — the formula needs an actual count to compute a window from

## When a Guessed Jitter Window Is Good Enough

- The key population is small and stable relative to backend capacity, so almost any spread keeps every tick under capacity without needing the formula
- The backend recompute is cheap enough that even a full concentrated burst is tolerable
- An independent circuit breaker or backend-side rate limiter already caps concurrent recompute calls regardless of how the cache behaves

## Code Examples in This Repo

| Example | Domain | What It Shows |
|---------|--------|--------------|
| [`pressure`](./pressure) | Fixed TTL, bulk warm-up | All 1000 keys expire at the same tick; peak of 1000 vs. capacity of 50 |
| [`wrong-direction`](./wrong-direction) | Random jitter, guessed window | Peak drops to 115 — a real improvement, still over the safe capacity of 50 |
| [`principle-applied`](./principle-applied) | Computed stagger window | Peak of exactly 50, guaranteed by `ceil(keyCount / capacity)` |

### How to Run

```bash
cd volume-22-caching-architecture-deep-dive/paper-117-cache-stampede/pressure
javac *.java && java Main

cd volume-22-caching-architecture-deep-dive/paper-117-cache-stampede/wrong-direction
javac *.java && java Main

cd volume-22-caching-architecture-deep-dive/paper-117-cache-stampede/principle-applied
javac *.java && java Main
```
