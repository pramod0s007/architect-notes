# Outbox Pattern — Making a Local Write and a Notification Atomic

**Pattern:** Outbox — write the business state change and the outgoing event in one local transaction; a separate relay publishes it reliably

**Continues Volume 9's running example**, picking up right after Paper 49 (Saga Pattern).

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

`InventoryService.reserve()` needs to both update its own state and notify `ShippingService` that a reservation happened. Doing that as two separate writes — update local state, then publish an event — means a failure between the two (a broker outage, a crash) leaves local state confirmed and the event permanently lost. This paper measures that dual-write problem directly, fixes it by making the event a durable row in the same local transaction as the state change (with a separate relay retrying delivery until confirmed), and shows the overcorrection: a relay that marks an event "published" before actually confirming the broker received it.

## The Pressure: The Local Write Succeeds, the Event Doesn't

Measured: with the broker unreachable at the moment of the call, `reserve()` still returns `true` and `isReserved()` still reports `true` — but `broker.receivedEvents()` is empty. Local state and the rest of the system now permanently disagree about what happened, with no exception thrown anywhere.

## The Principle

**Write the business state change and the event to the same local transaction; let a separate, retrying relay handle actually getting the event to the broker, marking it done only once confirmed.** Measured against a broker that fails twice and succeeds on the third attempt: the reservation completes immediately and safely, while the event sits in the outbox — unpublished — through two failed polls, then gets marked published only after the third poll actually succeeds.

## Pros

- The local write can never partially happen — verified: it's exactly as fast and safe as a plain single-service write, never blocked on the broker
- The event is durable from the moment the local transaction commits — no window where it can be silently dropped
- Publishing becomes a separate, retryable concern that can fail and recover without any risk to the underlying state

## Cons

- At-least-once delivery, not exactly-once — a downstream consumer still needs to handle duplicate events
- Requires a relay process (polling or CDC-based) running continuously
- The pattern's entire guarantee lives in one line — marking an entry published only *after* confirmation — and that line is easy to get backwards (measured directly in this paper's wrong-direction)

## When NOT to Use

- When the downstream can just query the source service's state directly instead of needing a pushed event
- When the consuming side can't tolerate at-least-once delivery and duplicate handling is genuinely infeasible
- Pure in-process, single-transaction workflows with nothing else to notify (see Start Simple)

## Code Examples in This Repo

| Example | Domain | What It Shows |
|---------|--------|--------------|
| [`evolution/`](./evolution/) | Order fulfillment (continuing Papers 44-49) | v1: one service, nothing downstream to notify (fine) -> v2: local write + separate event publish, broker down, event silently lost (measured) -> v3: outbox table + relay with retry, event never lost despite two failed publish attempts (measured) |
| [`wrong-direction/`](./wrong-direction/) | Broken relay | Relay marks an outbox entry published before confirming the broker received it — event lost forever despite the outbox table and relay process both existing (measured: `receivedEvents=[]` even after 3 polls) |

### How to Run

```bash
cd volume-9-distributed-systems-resilience/paper-50-outbox-pattern/evolution/v1
javac *.java && java Main

cd volume-9-distributed-systems-resilience/paper-50-outbox-pattern/evolution/v2
javac *.java && java Main

cd volume-9-distributed-systems-resilience/paper-50-outbox-pattern/evolution/v3
javac *.java && java Main

cd volume-9-distributed-systems-resilience/paper-50-outbox-pattern/wrong-direction
javac *.java && java Main
```
