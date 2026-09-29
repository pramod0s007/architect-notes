# Service Discovery — Alive Isn't the Same Claim as Healthy

**Pattern:** Static configuration → Heartbeat/TTL registry → Active health checks

**Volume 8b** (Networking & RPC Fundamentals) — a short bridge volume between Volume 8 (Concurrency Patterns) and Volume 9 (Distributed Systems Resilience). Each paper here uses an independent example, in the pre-Volume-9 style.

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

A hardcoded `StaticServiceLocator` returns the same three addresses forever — after a normal rolling redeploy retires one and brings up a replacement, one-third of calls fail against the retired address and the replacement never receives a request (verified). A heartbeat/TTL registry fixes this: instances self-register and renew a lease, and `resolve()` always reflects who's currently heartbeating. But a heartbeat only proves a process is running, not that it can serve requests — this paper then simulates an instance that never stops heartbeating while its own health checks fail for several ticks, showing the heartbeat-only registry keeps routing to it the whole time, while a registry that also tracks independent health-check results excludes it for exactly the sick window.

## The Pressure: A List That Was Correct Once

A hardcoded address list doesn't know about redeploys, crashes, or autoscaling — it's a snapshot trusted forever.

## The Principle

**Registration plus lease-based resolution replaces a snapshot with a live view.** A heartbeat proves "still running." Only an independent health check proves "still able to serve requests" — they are different facts and can diverge for as long as a dependency stays broken.

## Pros

- A heartbeat/TTL registry needs no redeploy-aware configuration — it reflects reality automatically
- A TTL's bounded staleness window absorbs transient network blips instead of overreacting to them
- Active health checks catch the "alive but broken" case that heartbeats structurally cannot

## Cons

- A TTL means eviction is never instant — there's always a bounded delay between a crash and removal
- Active health checks are another moving part that itself needs to be cheap and reliable
- An aggressive health check that times out under load can evict genuinely healthy instances during a traffic spike

## When NOT to Use

- Topology genuinely never changes (a single fixed-address instance) — no discovery mechanism is needed at all
- The heartbeat channel already exercises the real request-handling path — a separate health check would be redundant
- The health signal itself is more expensive or fragile than the failure it prevents

## Code Examples in This Repo

| Example | Domain | What It Shows |
|---------|--------|--------------|
| [`evolution/v1`](./evolution/v1) | 3 backends behind a static locator | Hardcoded list survives a redeploy — 1 of 3 calls hits a retired address, the replacement gets zero traffic |
| [`evolution/v2`](./evolution/v2) | Same redeploy scenario | Heartbeat/TTL registry: retired instance drops out on lease expiry, new instance appears on first heartbeat |
| [`evolution/v3`](./evolution/v3) | Same registry, one instance goes health-sick | Heartbeat-only registry keeps routing to a sick-but-heartbeating instance; health-checked registry excludes it precisely |

### How to Run

```bash
cd volume-8b-networking-rpc-fundamentals/paper-39-service-discovery/evolution/v1
javac *.java && java Main

cd volume-8b-networking-rpc-fundamentals/paper-39-service-discovery/evolution/v2
javac *.java && java Main

cd volume-8b-networking-rpc-fundamentals/paper-39-service-discovery/evolution/v3
javac *.java && java Main
```
