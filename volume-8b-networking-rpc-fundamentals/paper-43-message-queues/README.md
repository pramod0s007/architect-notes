# Message Queues — At-Least-Once Delivery Only Becomes Exactly-Once With Idempotency

**Pattern:** Direct synchronous call (no buffer against failure) → At-most-once queue (dequeue-then-process, still loses messages mid-crash) → At-least-once queue with idempotent consumer (redelivery fixes loss, idempotency fixes duplication)

**Volume 8b** (Networking & RPC Fundamentals) — a short bridge volume between Volume 8 (Concurrency Patterns) and Volume 9 (Distributed Systems Resilience). Each paper here uses an independent example, in the pre-Volume-9 style. This is the final paper in Volume 8b, closing the Papers 38-43 gap.

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

A direct synchronous call between a producer and consumer has no buffer against failure — a crash or unavailability in the consumer is an immediate, unrecoverable failure in the producer, and the work is simply gone (verified: order-2 dropped, no record it ever existed). Adding a plain queue with at-most-once delivery (`dequeue()` removes a message before it's processed) decouples availability, but not processing failures — a consumer crash between dequeue and finishing still loses the message permanently, just one layer removed from the direct-call failure (verified: order-2 gone from both queue and payment processor, queue depth 0). Switching to ack-based, at-least-once delivery (`peek()` + explicit `ack()`) fixes message loss — a crash before acking means the message is redelivered — but introduces duplicate processing if the consumer has no memory of what it already did (verified: order-2 charged twice). The actual fix is an idempotency guard in the consumer, tracking which order IDs were already processed, so a legitimate redelivery is detected and skipped rather than re-executed (verified: order-2 charged exactly once, queue behavior unchanged between both scenarios).

## The Pressure: A Direct Call Has No Buffer Against Failure

Tight coupling between producer and consumer means a consumer failure is an immediate producer failure — there's no partial-failure state, no "pending," just success or permanent loss.

## The Principle

**"Exactly-once" is a consumer property, not a queue property.** A message queue's job is to never lose a message once accepted; at-least-once delivery does that correctly by redelivering anything that wasn't acked. But redelivery and duplicate processing are the same event viewed from two different layers — the queue did its job correctly, and the correctness gap moves entirely into whether the consumer recognizes a redelivered message as a duplicate of work it already did.

## Pros

- A queue removes the producer's dependency on the consumer's real-time availability
- Ack-based redelivery guarantees a message is never silently lost once durably enqueued
- An idempotency guard, keyed on a stable identifier (an order ID, an idempotency token), converts at-least-once delivery into effectively-exactly-once processing without weakening the queue's guarantee

## Cons

- At-most-once delivery (dequeue-then-process) is simple but loses messages on any crash between dequeue and completion
- At-least-once delivery without an idempotency guard turns any consumer crash into a guaranteed duplicate side effect
- Idempotency has to be implemented per side effect, wherever the actual mutation happens — it cannot be bolted on generically at the queue layer

## When NOT to Use a Message Queue

- The caller genuinely needs a synchronous, immediate answer — a queue doesn't fit a request/response contract where the user is waiting for a definitive result right now
- The work isn't safely retriable or idempotent, and can't easily be made so — at-least-once delivery is the realistic default for most queues, and a consumer that can't tolerate redelivery will eventually double-process something
- The operational cost of running and monitoring a queue outweighs the coupling problem it solves, for low-volume, loss-tolerant background work

## Code Examples in This Repo

| Example | Domain | What It Shows |
|---------|--------|--------------|
| [`evolution/v1`](./evolution/v1) | Direct synchronous call | order-2's call fails and is permanently dropped — no queue, no buffer, no retry |
| [`evolution/v2`](./evolution/v2) | At-most-once queue (dequeue-then-process) | order-2 is dequeued, then the consumer crashes mid-processing — gone from both queue and payment processor |
| [`evolution/v3`](./evolution/v3) | At-least-once queue (ack-based) — with and without idempotency | Scenario 1: redelivery fixes message loss but causes a duplicate charge. Scenario 2: an `IdempotentConsumer` detects the redelivery and skips the duplicate |

### How to Run

```bash
cd volume-8b-networking-rpc-fundamentals/paper-43-message-queues/evolution/v1
javac *.java && java Main

cd volume-8b-networking-rpc-fundamentals/paper-43-message-queues/evolution/v2
javac *.java && java Main

cd volume-8b-networking-rpc-fundamentals/paper-43-message-queues/evolution/v3
javac *.java && java Main
```
