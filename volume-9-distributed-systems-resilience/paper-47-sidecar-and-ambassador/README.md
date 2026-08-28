# Sidecar & Ambassador — Resilience Without Duplication

**Pattern:** Sidecar / Ambassador — a helper process (or shared component) centralizing cross-cutting resilience concerns

**Continues Volume 9's running example**, picking up right after Paper 46 (Distributed Monolith).

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

`PaymentRouter`, `InventoryService`, and `ShippingService` each independently added retry logic for calling their own downstream — and got three different, uncoordinated answers to the same question. This paper measures the inconsistency directly (identical downstream flakiness, different outcomes depending on which service you ask), fixes it with one shared `ResilienceSidecar`, and shows the overcorrection: bespoke per-service ambassadors that just relocate the same duplication.

## The Pressure: Three Services, Three Retry Policies

Verified: with the exact same downstream flakiness (fails twice, succeeds on the third attempt), `InventoryService` (3 attempts allowed) succeeds and `ShippingService` (2 attempts allowed) fails — same question, two different uncoordinated answers.

## The Principle

**A sidecar or ambassador centralizes cross-cutting resilience concerns in one place, deployed consistently, instead of reimplemented inconsistently inside every service.** The same class, same configuration, applied identically everywhere it's needed.

## Pros

- Every service gets identical retry behavior, verified — same downstream flakiness now produces the same outcome regardless of which service is calling
- A resilience policy tuned after an incident can be updated in one place
- Deployable independently of each service's own release cycle (the sidecar-as-process form specifically)

## Cons

- Adds a process boundary and a local hop compared to an inline call
- A shared library achieves the same logical centralization without the process boundary — the choice is a real trade-off, not automatic
- Tempting to let the sidecar become service-specific again, recreating the exact duplication it was meant to remove

## When NOT to Use

- A single service, single downstream call — nothing to keep consistent yet
- When the resilience policy genuinely needs to differ per downstream for a real reason — that's configuration on a shared sidecar, not a reason for separate bespoke ones
- Extremely latency-sensitive paths where even a local hop's overhead matters

## Code Examples in This Repo

| Example | Domain | What It Shows |
|---------|--------|--------------|
| [`evolution/`](./evolution/) | Order fulfillment (continuing Papers 44-46) | v1: one service, inline retry (fine) -> v2: two services, same flakiness, different retry counts, different outcomes (measured) -> v3: one `ResilienceSidecar`, identical outcome for both |
| [`wrong-direction/`](./wrong-direction/) | Bespoke ambassadors | Two per-service ambassadors with their own retry counts — the duplication reappears, measured (Inventory succeeds, Shipping fails, same flakiness) |

### How to Run

```bash
cd volume-9-distributed-systems-resilience/paper-47-sidecar-and-ambassador/evolution/v1
javac *.java && java Main

cd volume-9-distributed-systems-resilience/paper-47-sidecar-and-ambassador/evolution/v2
javac *.java && java Main

cd volume-9-distributed-systems-resilience/paper-47-sidecar-and-ambassador/evolution/v3
javac *.java && java Main

cd volume-9-distributed-systems-resilience/paper-47-sidecar-and-ambassador/wrong-direction
javac *.java && java Main
```
