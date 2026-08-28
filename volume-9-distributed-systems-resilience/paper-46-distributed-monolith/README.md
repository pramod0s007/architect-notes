# Distributed Monolith — Microservices in Name Only

**Anti-pattern:** Distributed Monolith — services physically split but still coupled by shared data and synchronous call chains

**Continues Volume 9's running example**, picking up right after Paper 45 (API Gateway).

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

Behind the gateway from Paper 45 are three separately deployed services — but they all read and write the same `orders` table, and they call each other synchronously in a fixed chain. Extracting the codebase without extracting the data or the call structure buys network latency and deployment complexity for none of the actual benefit. This paper measures both problems directly and fixes one of them, deliberately leaving the other for Paper 48.

## The Pressure: One Slow Step's Delay Becomes the Whole Request's Delay

Measured: a single order with `InventoryService` slowed to 3 seconds takes ~3000ms end to end, even though Payment and Shipping are both instant — because `ApiGateway` waits for each step before calling the next.

## The Principle

**Two signals define a distributed monolith: shared data across services, and a synchronous call chain where one slow step blocks everything.** Fixing data ownership (this paper) removes the schema-coupling risk. It does not remove the latency problem — that's a structurally separate issue.

## Pros

- Independent data ownership means a schema change in one service literally cannot break another
- Measuring the two signals directly (shared tables, chain latency) gives an objective test instead of a vague "does it feel coupled" judgment
- Fixing data ownership is a real, necessary step even though it isn't sufficient alone

## Cons

- Splitting data without fixing the synchronous call chain still leaves the latency problem completely unsolved (measured: same ~3000ms before and after)
- A temporary shared-database period during migration is normal — teams sometimes treat it as the finish line instead
- Org-chart milestones (separate repos, deploys, on-call) can all be hit while both defining signals of this anti-pattern remain true

## When a Distributed Monolith Might Be an Acceptable Stopping Point

- Mid-migration, deliberately, as a known and tracked state
- When true independent deployability isn't actually needed for services always deployed together anyway
- Never as a permanent architecture

## Code Examples in This Repo

| Example | Domain | What It Shows |
|---------|--------|--------------|
| [`evolution/`](./evolution/) | Order fulfillment (continuing Papers 44-45) | v1: a service with its own data store (fine) -> v2: shared `orders` table + synchronous chain, one slow step measured at ~3000ms request latency -> v3: data ownership fixed, latency measured STILL ~3000ms — proving data ownership alone doesn't fix it |

### How to Run

```bash
cd volume-9-distributed-systems-resilience/paper-46-distributed-monolith/evolution/v1
javac *.java && java Main

cd volume-9-distributed-systems-resilience/paper-46-distributed-monolith/evolution/v2
javac *.java && java Main

cd volume-9-distributed-systems-resilience/paper-46-distributed-monolith/evolution/v3
javac *.java && java Main
```
