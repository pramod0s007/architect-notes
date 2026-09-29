# Load Balancing Strategies — Static Weights Trust a Number That's Gone Stale

**Pattern:** Round Robin → Weighted Round Robin → Dynamic Least-Load balancing

**Opens Volume 8b** (Networking & RPC Fundamentals) — a short bridge volume between Volume 8 (Concurrency Patterns) and Volume 9 (Distributed Systems Resilience). Each paper here uses an independent example, in the pre-Volume-9 style.

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

A round-robin load balancer split 30 requests evenly across three backends rated at capacities 10, 5, and 1 — every backend got exactly 10 requests, and the slowest backend (capacity 1) set the pace for the entire batch, forcing a total completion time of 10 ticks. Weighted round robin fixes this by splitting proportional to real capacity (verified: makespan drops from 10 ticks to 2). But its weights are a snapshot taken once — this paper then simulates a backend's capacity degrading at runtime and shows the static-weighted balancer's backlog growing tick over tick while a balancer that reads live load stays at zero throughout.

## The Pressure: One Counter, No Notion of Capacity

Round robin cycles through backends in fixed order regardless of what each one can actually process — fine when backends are identical, a bottleneck the moment they're not.

## The Principle

**A load balancer's decision is only as good as the information it's allowed to use.** Round robin uses none. Weighted round robin uses a static snapshot. Dynamic least-load uses live state, re-read on every decision — the only one of the three that can react to a backend's real capacity changing after the balancer was configured.

## Pros

- Weighted round robin is cheap (fixed weights, computed once) and correct as long as capacity truly is stable
- Dynamic least-load never lets a backlog form during a runtime degradation, because it never trusts a stale number
- Largest-remainder apportionment keeps a weighted split's requests fully accounted for — no silently dropped remainder

## Cons

- Dynamic tracking requires live per-backend state and a signal that's cheap enough to read on every request
- A noisy or expensive "live load" signal can cost more than the misrouting it prevents
- Weighted round robin's weights need a redeploy (or an explicit reconfiguration) to reflect real capacity changes — it will not self-correct

## When NOT to Use

- All backends are genuinely identical and stay that way — plain round robin is correct and cheapest
- Backends differ in capacity, but that capacity is stable — weighted round robin is the right level of sophistication
- The live-load signal itself is noisy or expensive to gather — a cheaper proxy may beat exact live tracking

## Code Examples in This Repo

| Example | Domain | What It Shows |
|---------|--------|--------------|
| [`evolution/v1`](./evolution/v1) | 3 backends, capacities 10/5/1 | Round robin: same 10 requests per backend regardless of capacity — makespan 10 ticks |
| [`evolution/v2`](./evolution/v2) | Same 3 backends | Weighted round robin: capacity-proportional split via largest-remainder apportionment — makespan 2 ticks |
| [`evolution/v3`](./evolution/v3) | Same 3 backends, backend 0 degrades at runtime | Static-weighted balancer's backlog grows during degradation; dynamic least-load balancer's backlog stays at zero |

### How to Run

```bash
cd volume-8b-networking-rpc-fundamentals/paper-38-load-balancing-strategies/evolution/v1
javac *.java && java Main

cd volume-8b-networking-rpc-fundamentals/paper-38-load-balancing-strategies/evolution/v2
javac *.java && java Main

cd volume-8b-networking-rpc-fundamentals/paper-38-load-balancing-strategies/evolution/v3
javac *.java && java Main
```
