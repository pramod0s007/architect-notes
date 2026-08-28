# API Gateway — One Front Door for a System in Transition

**Pattern:** API Gateway — single entry point hiding backend topology from clients

**Continues Volume 9's running example**, picking up right after Paper 44 (Strangler Fig).

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

Paper 44 extracted a payment provider behind a router that lives entirely inside the monolith — no external caller needed to know. But once Inventory and Shipping get extracted too, every client of the system (mobile app, web app, admin tool) has to independently know all three services, in what order to call them, and how to handle each one's failure. This paper puts a single gateway in front of all three, and shows why business logic creeping into that gateway recreates the exact coupling problem the extraction was meant to fix.

## The Pressure: Every Client Re-Implements the Same Orchestration

Verified: `MobileAppClient` and `WebAppClient`, written independently, return different messages (`"REJECTED: payment failed"` vs. `"Payment declined"`) for the *identical* payment failure — because each one wired up its own copy of the same three-service call sequence.

## The Principle

**A gateway is the one place that knows the current backend topology.** Clients call one address; if the topology behind it changes, the gateway changes once, and no client needs to know anything happened.

## Pros

- Clients stay simple and never need to change when backend topology changes
- Orchestration logic lives in exactly one place instead of duplicated (and drifting) across every consumer
- Failure handling becomes consistent across all clients, verified in this paper's own demo

## Cons

- Adds a network hop and a piece of infrastructure that isn't needed with a single backend
- Everything behind the gateway is now reachable only through it — a gateway outage is a full outage
- Tempting to add "just this one business rule" to the gateway, which starts the exact coupling problem the extraction was meant to solve

## When NOT to Use

- A single backend service — nothing to hide yet
- When the gateway would need to make business decisions rather than just route
- Internal service-to-service calls that don't cross a trust or client boundary

## Code Examples in This Repo

| Example | Domain | What It Shows |
|---------|--------|--------------|
| [`evolution/`](./evolution/) | Order fulfillment (continuing Paper 44) | v1: one backend, direct call (fine) -> v2: two independently-written clients drift on the same failure message (measured) -> v3: one `ApiGateway`, both clients return identical messages |
| [`wrong-direction/`](./wrong-direction/) | VIP discount + fraud rule in the gateway | A business rule invisible outside the gateway causes a real reconciliation mismatch, measured ($100 expected vs. $90 actual) |

### How to Run

```bash
cd volume-9-distributed-systems-resilience/paper-45-api-gateway/evolution/v1
javac *.java && java Main

cd volume-9-distributed-systems-resilience/paper-45-api-gateway/evolution/v2
javac *.java && java Main

cd volume-9-distributed-systems-resilience/paper-45-api-gateway/evolution/v3
javac *.java && java Main

cd volume-9-distributed-systems-resilience/paper-45-api-gateway/wrong-direction
javac *.java && java Main
```
