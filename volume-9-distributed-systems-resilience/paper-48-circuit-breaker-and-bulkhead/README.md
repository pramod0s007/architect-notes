# Circuit Breaker & Bulkhead — Failing Fast, Failing Contained

**Pattern:** Circuit Breaker (stop calling a dependency that's clearly dead) + Bulkhead (isolate resource pools per dependency)

**Continues Volume 9's running example**, picking up right after Paper 47 (Sidecar & Ambassador).

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

`PaymentRouter` keeps retrying a completely dead payment backend through the shared `ResilienceSidecar` from Paper 47. Measured: on a shared thread pool, this starves unrelated, healthy work (already-paid orders needing only Inventory + Shipping) to 40-60x their normal latency. A circuit breaker and a bulkhead look like they solve the same problem — this paper measures them separately and finds they don't.

## The Pressure: A Dead Dependency Starves a Shared Pool

Measured: already-paid orders (healthy dependencies, ~10ms of real work) took max=605ms, avg=590ms on a pool shared with orders retrying a dead payment backend — not because inventory or shipping were slow, but because the pool's threads were occupied by payment's retries.

## The Principle

**A circuit breaker stops you from calling something that's already dead. A bulkhead stops one dependency's problem from spreading to unrelated work, regardless of whether you're still calling it.** Measured separately: a circuit breaker alone barely helped (590ms vs. 605ms) against a concurrent burst that all committed to its retry cost before the breaker had enough signal to trip. A bulkhead alone fully protected unrelated work (25ms) — structurally, not probabilistically, since payment's threads could never be fulfillment's threads.

## Pros

- Circuit breaker: stops wasting the failing dependency's own resources on retries against something that isn't coming back
- Bulkhead: guarantees other dependencies stay fast regardless of what one dependency is doing — verified even when the failing dependency retries forever
- Together: fail fast AND contain the blast radius — the combination Hystrix popularized per-dependency

## Cons

- A circuit breaker's protection is asymptotic — it does nothing for calls already in flight when the failures that trip it happen (measured directly)
- A bulkhead sized without real traffic data can reject legitimate load as if it were the failure it's meant to contain
- Sharing a breaker's *instance* (not just its class) across unrelated dependencies conflates their health into one meaningless counter (measured: a fully healthy dependency rejected 5/5 times because of an unrelated dependency's failures)

## When NOT to Use

- Low-traffic, low-concurrency paths with no shared pool to starve
- A dependency with no fallback and no shared pool at risk — fail-fast mainly matters for protecting the pool, not necessarily user-facing latency
- Bulkheads sized as a guess rather than from measured traffic

## Code Examples in This Repo

| Example | Domain | What It Shows |
|---------|--------|--------------|
| [`evolution/`](./evolution/) | Order fulfillment (continuing Papers 44-47) | v1: one order, one dependency, no pool to starve (fine) -> v2: shared pool + dead payment backend starves healthy work (measured 605ms vs. ~10ms) -> v3: circuit breaker alone (measured, barely helps against a burst) vs. bulkhead alone (measured, fully protects) vs. both together |
| [`wrong-direction/`](./wrong-direction/) | Shared breaker instance | `PaymentRouter` and `InventoryService` share the same `CircuitBreaker` instance — Payment's failures trip it, and a completely healthy Inventory backend gets rejected 5/5 times, verified with a real invocation counter |

### How to Run

```bash
cd volume-9-distributed-systems-resilience/paper-48-circuit-breaker-and-bulkhead/evolution/v1
javac *.java && java Main

cd volume-9-distributed-systems-resilience/paper-48-circuit-breaker-and-bulkhead/evolution/v2
javac *.java && java Main

cd volume-9-distributed-systems-resilience/paper-48-circuit-breaker-and-bulkhead/evolution/v3
javac *.java && java Main

cd volume-9-distributed-systems-resilience/paper-48-circuit-breaker-and-bulkhead/wrong-direction
javac *.java && java Main
```
