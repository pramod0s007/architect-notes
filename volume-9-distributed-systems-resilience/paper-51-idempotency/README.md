# Idempotency — Closing the Loop on At-Least-Once

**Pattern:** Idempotency — an operation that produces the same result whether performed once or multiple times, made safe via a stable dedup key

**Closes Volume 9's running example** — the final paper in the arc, picking up right after Paper 50 (Outbox Pattern).

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

Paper 50's outbox relay guarantees an event is delivered at least once — which means it can be delivered more than once. This paper measures what happens when the receiving handler has no memory of what it already processed (the same event, delivered twice, becomes two physical shipments), fixes it with a handler that tracks a stable per-event ID and skips anything already seen, and shows the overcorrection: dedup code that checks against a key regenerated on every call, which can never actually match anything.

## The Pressure: Redelivery Turns Into Reprocessing

Measured: the same `OrderEvent` (same `eventId`) delivered twice to a naive handler produces `shipmentCount=2`. Neither call did anything wrong on its own — the handler simply has no way to tell "new event" from "event I already handled."

## The Principle

**Idempotency is a property of whether the caller can recognize it already did something specific, not a property of the operation's own code.** Measured: wrapping the same `ShippingService.schedule()` with a handler that tracks `event.getEventId()` — a key minted once at publish time and carried unchanged through every redelivery — reduces two deliveries to exactly one shipment, with the second call cleanly skipped.

## Pros

- Verified: makes at-least-once delivery (Paper 47's retries, Paper 50's outbox relay) safe to build on top of, instead of dangerous
- The fix lives in a layer in front of the side effect — the side-effecting code itself doesn't need to change
- Works regardless of *why* a duplicate arrived (retry, redelivery, network timeout that actually succeeded)

## Cons

- Only as reliable as the stability of the dedup key — verified: a key regenerated per call reproduces the exact same double-processing as no dedup at all
- An in-memory dedup set doesn't survive a restart or scale across multiple consumer instances without externalizing it
- The check-then-record-then-act sequence itself needs to be atomic under concurrent duplicate deliveries, or two duplicates can both pass the check before either is recorded

## When NOT to Worry About It

- Delivery that's genuinely, verifiably exactly-once at the infrastructure layer
- Operations that are naturally idempotent already (an overwrite like `Map.put`, not an accumulation like scheduling a new shipment)
- Read-only operations with no side effect to duplicate

## Code Examples in This Repo

| Example | Domain | What It Shows |
|---------|--------|--------------|
| [`evolution/`](./evolution/) | Order fulfillment (continuing Papers 44-50) | v1: one delivery, nothing to deduplicate (fine) -> v2: same event delivered twice, naive handler double-ships (measured: shipmentCount=2) -> v3: handler tracks stable eventId, second delivery skipped (measured: shipmentCount=1) |
| [`wrong-direction/`](./wrong-direction/) | Broken dedup key | Handler has a `Set` and a lookup, but the key is regenerated every call instead of using the event's own identity — the dedup check never fires (measured: shipmentCount=2, identical to no dedup at all) |

### How to Run

```bash
cd volume-9-distributed-systems-resilience/paper-51-idempotency/evolution/v1
javac *.java && java Main

cd volume-9-distributed-systems-resilience/paper-51-idempotency/evolution/v2
javac *.java && java Main

cd volume-9-distributed-systems-resilience/paper-51-idempotency/evolution/v3
javac *.java && java Main

cd volume-9-distributed-systems-resilience/paper-51-idempotency/wrong-direction
javac *.java && java Main
```
