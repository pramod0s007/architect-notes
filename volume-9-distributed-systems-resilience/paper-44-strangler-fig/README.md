# Strangler Fig — Replacing a Monolith Without a Big-Bang Rewrite

**Pattern:** Strangler Fig (Martin Fowler, 2004) — incremental replacement via a routing seam

**Opens Volume 9's running example:** an order-fulfillment system, evolved across all 8 papers in this volume. Each paper's fix creates the next paper's problem.

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

A monolith doesn't get replaced in one big rewrite — it gets circled and gradually starved of responsibility. This paper extracts a payment provider from `OrderFulfillmentMonolith` using a routing facade, showing why a global hardcoded switch fails (every customer moves at once, no fallback) and why duplicating the routing rule across call sites is worse (verified: the same customer's charge and refund land on two different providers).

## The Pressure: Two Providers, No Way to Choose

A hardcoded `USE_MODERN_FOR_EVERYONE` boolean is the entire "routing" logic — every order goes through the same provider, and changing it means every customer moves at once, with no fallback.

## The Principle

**A routing facade decides per-request which implementation (old or new) handles it — the seam needs exactly one home.** The monolith keeps running underneath, unaware anything has changed underneath it.

## Pros

- No big-bang cutover — a bug in the new implementation only affects the traffic routed to it
- The migration's halfway state is a legitimate, ongoing architecture, not something to rush past
- Routing granularity (per-customer, per-region, percentage-based) is entirely up to the router — easy to adjust without touching either implementation

## Cons

- Running two implementations simultaneously costs real maintenance overhead during the transition
- If old and new implementations can't safely coexist (incompatible shared state), the pattern needs that solved first
- A router built before there's a second implementation to route to is speculative infrastructure

## When NOT to Use

- A system small enough to rewrite safely in one sitting
- When partial migration would corrupt shared state
- A one-time data migration with no ongoing dual-write need

## Code Examples in This Repo

| Example | Domain | What It Shows |
|---------|--------|--------------|
| [`evolution/`](./evolution/) | Order fulfillment monolith | v1: one payment path (fine) -> v2: two providers exist, only a global hardcoded switch (inflexible) -> v3: `PaymentRouter` decides per-order, no redeploy needed |
| [`wrong-direction/`](./wrong-direction/) | Charge/refund duplication | The routing rule copy-pasted into two services drifts — verified: same customer, two different providers |

### How to Run

```bash
cd volume-9-distributed-systems-resilience/paper-44-strangler-fig/evolution/v1
javac *.java && java Main

cd volume-9-distributed-systems-resilience/paper-44-strangler-fig/evolution/v2
javac *.java && java Main

cd volume-9-distributed-systems-resilience/paper-44-strangler-fig/evolution/v3
javac *.java && java Main

cd volume-9-distributed-systems-resilience/paper-44-strangler-fig/wrong-direction
javac *.java && java Main
```
