# Rate Limiting — A Per-Instance Limit Is Not a Global Limit

**Pattern:** Fixed-window counter (boundary burst) → Sliding-window log (fixes the burst) → Per-instance drift across a load-balanced deployment → Shared/centralized limiter

**Volume 8b** (Networking & RPC Fundamentals) — a short bridge volume between Volume 8 (Concurrency Patterns) and Volume 9 (Distributed Systems Resilience). Each paper here uses an independent example, in the pre-Volume-9 style.

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

A fixed-window rate limiter resets its counter to zero at every window boundary — clustering 5 requests just before the boundary and 5 more just after it lets 10 requests through in a 180ms span, roughly double the configured "5 per 1000ms" limit (verified). A sliding-window log fixes this by keeping an exact timestamp history instead of a resettable integer, correctly rejecting the same burst (verified). But a correct per-instance limiter, run independently across 3 load-balanced app instances, enforces a correct *per-instance* limit — 15 requests get through against a global cap of 5 (verified). The fix isn't a smarter algorithm, it's making the counter itself shared across instances (verified).

## The Pressure: A Counter That Forgets Everything at the Boundary

A fixed window limiter answers "at most N per calendar-aligned window," not "at most N in any trailing window" — and the gap between those two guarantees is exactly one clock boundary wide.

## The Principle

**Rate limiting is really two independent decisions: which counting algorithm, and where does its state actually live.** A correct algorithm (even one with zero boundary imprecision) still enforces N times the intended limit if it's run independently across N instances instead of sharing one counter.

## Pros

- A sliding-window log gives exact, boundary-free rate enforcement with a straightforward algorithm
- A shared/centralized limiter makes the global limit actually global, regardless of instance count or load-balancing strategy
- The counting algorithm and the state topology are cleanly separable — fixing one doesn't require touching the other

## Cons

- A sliding-window log costs memory proportional to request volume, not a single integer
- A shared limiter adds a network round-trip and a new dependency (e.g. Redis) to every request's critical path
- A fixed-window counter's boundary burst is easy to miss in testing if traffic is generated evenly rather than clustered

## When NOT to Use (a shared limiter)

- The limit is genuinely meant to be per-instance (e.g. "each replica may use at most 5 connections to its own local resource")
- The shared store's own latency/availability would hurt more than an occasionally-blown local limit for a soft, best-effort cap
- Traffic never clusters near a window boundary in practice and a fixed window's imprecision is never actually exercised

## Code Examples in This Repo

| Example | Domain | What It Shows |
|---------|--------|--------------|
| [`evolution/v1`](./evolution/v1) | Fixed-window counter | Boundary burst: 10 requests allowed in a 180ms span against a 5-per-1000ms limit |
| [`evolution/v2`](./evolution/v2) | Sliding-window log | Same burst timeline correctly capped at 5 |
| [`evolution/v3`](./evolution/v3) | Multi-instance deployment | Per-instance limiters blow the global cap 3x; a shared limiter fixes it |

### How to Run

```bash
cd volume-8b-networking-rpc-fundamentals/paper-41-rate-limiting/evolution/v1
javac *.java && java Main

cd volume-8b-networking-rpc-fundamentals/paper-41-rate-limiting/evolution/v2
javac *.java && java Main

cd volume-8b-networking-rpc-fundamentals/paper-41-rate-limiting/evolution/v3
javac *.java && java Main
```
