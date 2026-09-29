# Paper 164: Nova's Order-to-Fulfillment Data Path

Part of [Architect Notes](../../README.md) — Volume 30: Capstone: One System, End to End.

This is the third paper of Volume 30, continuing directly from Nova, the fictional multi-tenant
order/checkout and fulfillment platform carried through Papers 162 (data-layer architecture) and
163 (API boundary evolution). This paper moves to what happens after an order is accepted — the
write path from Nova's Orders store to its Inventory store. It ties to Volume 23 (Database
Internals for Architects) and Volume 24 (Event-Driven Architecture Patterns).

## The Question

Placing an order in Nova updates two separate stores: the Orders store records the order, and the
Inventory store decrements the stock it consumed. When these are two direct, uncoordinated writes
and the second one fails, Nova is left with a permanent inconsistency. The outbox pattern fixes the
"the write was never retried" half of that problem — but an outbox's delivery guarantee is
at-least-once, not exactly-once, which opens a different gap: the same event can be delivered to a
consumer twice. What closes both gaps together?

## Structure

- `pressure/` — Two direct, uncoordinated writes to the Orders and Inventory stores. Verified: the
  order write commits, the inventory write fails (simulated fault), and nothing coordinates or
  retries — the order is PLACED while inventory stays at 50 units instead of dropping to 45.
- `wrong-direction/` — An outbox event is committed alongside the order write (modeled as one local
  transaction), guaranteeing the inventory-decrement intent is never lost. Verified: the consumer
  applies the decrement, a simulated crash loses the acknowledgment, and the redelivered event is
  reapplied — inventory ends at 40 instead of 45, a double-decrement.
- `principle-applied/` — The same outbox pattern, paired with an idempotent consumer that tracks
  processed event IDs. Verified: the same redelivery happens, but the consumer recognizes it and
  skips reapplying — inventory ends at 45, the correct result for this scenario. The paper is
  explicit about what a production version of this still needs beyond what's modeled: the decrement
  and the processed-event-ID record must commit together, atomically.

## Running It

Each scenario is self-contained:

```bash
javac -Xlint:all *.java
java Main
```

## Full Write-Up

See [MEDIUM-DRAFT.md](../../../architect-notes-private/volume-30-capstone-one-system-end-to-end/paper-164-nova-order-fulfillment-data-path/MEDIUM-DRAFT.md)
(private repo) for the full analysis, comparison table, and interview Q&A — including which parts
of the outbox-plus-idempotent-consumer pattern generalize to external side effects (payment
captures, notifications) and which don't.
